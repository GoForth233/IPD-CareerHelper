package com.group1.career.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.repository.InterviewQuestionRepository;
import com.group1.career.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * F28a: Weekly AI question generation job.
 *
 * <p>Runs every Monday at 03:00. Generates new interview questions using
 * qwen-turbo for the top interview positions in the system. Generated questions
 * are stored with source=AI_GENERATED and review_status=PENDING_REVIEW, so
 * admins must approve them before they appear in the public market.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionAiGenerationJob {

    private static final String TURBO_MODEL = "qwen-turbo";
    private static final int QUESTIONS_PER_POSITION = 5;

    private static final List<String> DEFAULT_POSITIONS = List.of(
            "Java Backend Developer",
            "Frontend Developer (Vue)",
            "Backend Developer (MySQL)",
            "Spring Backend Developer",
            "Backend Developer (Redis)"
    );

    private final AiService aiService;
    private final InterviewQuestionRepository questionRepo;
    private final ObjectMapper objectMapper;

    /**
     * Runs every Monday at 03:00 (avoids peak hours).
     */
    @Scheduled(cron = "0 0 3 * * MON")
    @Transactional
    public void run() {
        log.info("[QuestionAiGenerationJob] Starting weekly question generation for {} positions",
                DEFAULT_POSITIONS.size());

        int totalSaved = 0;
        for (String position : DEFAULT_POSITIONS) {
            try {
                List<InterviewQuestion> generated = generateQuestionsForPosition(position);
                questionRepo.saveAll(generated);
                totalSaved += generated.size();
                log.info("[QuestionAiGenerationJob] Saved {} questions for position '{}'",
                        generated.size(), position);
            } catch (Exception e) {
                log.error("[QuestionAiGenerationJob] Failed to generate questions for '{}': {}",
                        position, e.getMessage(), e);
            }
        }

        log.info("[QuestionAiGenerationJob] Completed. Total questions saved for review: {}", totalSaved);
    }

    private List<InterviewQuestion> generateQuestionsForPosition(String position) {
        String systemPrompt = "你是一位资深面试官，专门出题用于技术面试题库。";
        String userPrompt = String.format(
                "请为「%s」岗位生成 %d 道面试题，涵盖不同难度（Easy/Normal/Hard）。\n" +
                "每道题必须严格按以下 JSON 数组格式输出，不要有任何额外文字：\n" +
                "[\n" +
                "  {\"content\": \"题目内容\", \"difficulty\": \"Easy|Normal|Hard\", \"answer\": \"参考答案（简洁，不超过200字）\"}\n" +
                "]\n" +
                "要求：题目内容具体、有价值；答案简洁、准确；不要重复市面上过于常见的题目。",
                position, QUESTIONS_PER_POSITION
        );

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        String response = aiService.chat(messages, TURBO_MODEL);
        return parseGeneratedQuestions(response, position);
    }

    private List<InterviewQuestion> parseGeneratedQuestions(String response, String position) {
        List<InterviewQuestion> result = new ArrayList<>();
        try {
            // Extract JSON array from response (model may include extra text)
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']');
            if (start == -1 || end == -1 || end <= start) {
                log.warn("[QuestionAiGenerationJob] No valid JSON array found in response for '{}'. Response: {}",
                        position, response.substring(0, Math.min(200, response.length())));
                return result;
            }
            String json = response.substring(start, end + 1);
            JsonNode arr = objectMapper.readTree(json);
            if (!arr.isArray()) return result;

            for (JsonNode node : arr) {
                String content = node.path("content").asText("").trim();
                String difficulty = node.path("difficulty").asText("Normal").trim();
                String answer = node.path("answer").asText("").trim();

                if (content.isEmpty()) continue;
                if (!List.of("Easy", "Normal", "Hard").contains(difficulty)) difficulty = "Normal";

                result.add(InterviewQuestion.builder()
                        .position(position)
                        .difficulty(difficulty)
                        .content(content)
                        .answer(answer.isEmpty() ? null : answer)
                        .source("AI_GENERATED")
                        .reviewStatus("PENDING_REVIEW")
                        .status("APPROVED")
                        .build());
            }
        } catch (Exception e) {
            log.error("[QuestionAiGenerationJob] Failed to parse AI response for '{}': {}", position, e.getMessage());
        }
        return result;
    }
}
