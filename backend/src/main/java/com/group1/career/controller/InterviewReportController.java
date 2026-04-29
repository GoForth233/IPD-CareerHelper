package com.group1.career.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.service.AiService;
import com.group1.career.service.InterviewService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Post-interview review report.
 *
 * The first request for an interview that has at least one user-turn triggers a
 * single AI call which returns structured JSON (radar scores, strengths,
 * improvements, summary). The result is cached on the Interview row
 * (report_json + final_score), so subsequent views are free.
 */
@Slf4j
@Tag(name = "Interview Report API", description = "Post-interview review report generation")
@RestController
@RequestMapping("/api/interviews/report")
@RequiredArgsConstructor
public class InterviewReportController {

    private final InterviewService interviewService;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "Get (or generate) the AI evaluation report for an interview")
    @GetMapping("/{interviewId}")
    public Result<InterviewReportDto> generateReport(@PathVariable Long interviewId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        Interview interview = interviewService.assertOwnership(interviewId, uid);

        // 1) Fast path: cached
        if (interview.getReportJson() != null && !interview.getReportJson().isBlank()) {
            try {
                InterviewReportDto cached = objectMapper.readValue(interview.getReportJson(), InterviewReportDto.class);
                return Result.success(cached);
            } catch (JsonProcessingException e) {
                log.warn("Cached report JSON for interview {} is corrupt; regenerating", interviewId, e);
            }
        }

        // 2) Build transcript
        List<InterviewMessage> messages = interviewService.getInterviewMessages(interviewId);
        long userTurns = messages.stream().filter(m -> "USER".equalsIgnoreCase(m.getRole())).count();
        if (userTurns == 0) {
            throw new BizException("Interview has no candidate answers yet — cannot evaluate");
        }

        StringBuilder transcript = new StringBuilder();
        for (InterviewMessage msg : messages) {
            String tag = "USER".equalsIgnoreCase(msg.getRole()) ? "Candidate" : "Interviewer";
            transcript.append("[").append(tag).append("] ").append(msg.getContent()).append("\n");
        }

        // 3) Single AI call -> structured JSON
        String prompt = buildEvaluationPrompt(
                interview.getPositionName(),
                interview.getDifficulty() == null ? "Normal" : interview.getDifficulty(),
                transcript.toString()
        );

        long t0 = System.currentTimeMillis();
        String raw = aiService.chat(prompt);
        log.info("[report] AI evaluation took {} ms for interview {}", System.currentTimeMillis() - t0, interviewId);

        InterviewReportDto report = parseReport(raw, interviewId, interview, (int) userTurns);

        // 4) Cache + persist final_score
        try {
            String json = objectMapper.writeValueAsString(report);
            interviewService.saveReport(interviewId, json, report.getOverallScore());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize report for interview {}", interviewId, e);
        }

        return Result.success(report);
    }

    // ============================================================
    private String buildEvaluationPrompt(String position, String difficulty, String transcript) {
        return "You are a senior hiring manager. Evaluate the candidate based ONLY on the transcript " +
               "below. Return STRICT JSON (no markdown, no prose) matching this exact shape:\n" +
               "{\n" +
               "  \"overallScore\": <int 0-100>,\n" +
               "  \"radarChart\": {\n" +
               "    \"expression\": <int 0-100>,\n" +
               "    \"logic\": <int 0-100>,\n" +
               "    \"technical\": <int 0-100>,\n" +
               "    \"pressureResistance\": <int 0-100>,\n" +
               "    \"communication\": <int 0-100>\n" +
               "  },\n" +
               "  \"strengths\": [ {\"title\": \"...\", \"detail\": \"...\"} ],\n" +
               "  \"improvements\": [ {\"title\": \"...\", \"detail\": \"...\"} ],\n" +
               "  \"summary\": \"2-3 sentence overall verdict.\"\n" +
               "}\n\n" +
               "Provide 1-3 strengths and 1-3 improvements. Be specific and reference what " +
               "the candidate actually said. Keep each detail under 200 characters.\n\n" +
               "=== Position ===\n" + position + " (difficulty: " + difficulty + ")\n\n" +
               "=== Transcript ===\n" + transcript;
    }

    private InterviewReportDto parseReport(String raw, Long interviewId, Interview interview, int userTurns) {
        if (raw == null) raw = "";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int firstNl = cleaned.indexOf('\n');
            if (firstNl > 0) cleaned = cleaned.substring(firstNl + 1);
            int closing = cleaned.lastIndexOf("```");
            if (closing > 0) cleaned = cleaned.substring(0, closing);
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        try {
            JsonNode node = objectMapper.readTree(cleaned);
            RadarChartData radar = RadarChartData.builder()
                    .expression(intField(node, "radarChart", "expression", 70))
                    .logic(intField(node, "radarChart", "logic", 70))
                    .technical(intField(node, "radarChart", "technical", 70))
                    .pressureResistance(intField(node, "radarChart", "pressureResistance", 70))
                    .communication(intField(node, "radarChart", "communication", 70))
                    .build();

            int overall = node.path("overallScore").asInt(
                    (radar.getExpression() + radar.getLogic() + radar.getTechnical()
                            + radar.getPressureResistance() + radar.getCommunication()) / 5);

            return InterviewReportDto.builder()
                    .interviewId(interviewId)
                    .positionName(interview.getPositionName())
                    .difficulty(interview.getDifficulty())
                    .durationSeconds(interview.getDurationSeconds())
                    .overallScore(Math.max(0, Math.min(100, overall)))
                    .totalQuestions(userTurns)
                    .radarChart(radar)
                    .strengths(toAdvice(node.path("strengths")))
                    .improvements(toAdvice(node.path("improvements")))
                    .textSummary(node.path("summary").asText(""))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI report JSON for interview {}: {}", interviewId, raw, e);
            throw new BizException("AI returned an unparseable evaluation. Please retry.");
        }
    }

    private int intField(JsonNode root, String parent, String key, int fallback) {
        JsonNode p = root.path(parent);
        if (p.isMissingNode()) return fallback;
        int v = p.path(key).asInt(fallback);
        return Math.max(0, Math.min(100, v));
    }

    private List<AdviceItem> toAdvice(JsonNode arr) {
        List<AdviceItem> out = new ArrayList<>();
        if (arr == null || !arr.isArray()) return out;
        for (JsonNode it : arr) {
            String title = it.path("title").asText("");
            String detail = it.path("detail").asText("");
            if (!title.isBlank() || !detail.isBlank()) {
                out.add(new AdviceItem(title, detail));
            }
        }
        return out;
    }

    // ================= DTO Classes =================

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class InterviewReportDto {
        private Long interviewId;
        private String positionName;
        private String difficulty;
        private Integer durationSeconds;
        private int overallScore;
        private int totalQuestions;
        private RadarChartData radarChart;
        private List<AdviceItem> strengths;
        private List<AdviceItem> improvements;
        private String textSummary;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class RadarChartData {
        private int expression;
        private int logic;
        private int technical;
        private int pressureResistance;
        private int communication;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class AdviceItem {
        private String title;
        private String detail;
    }
}
