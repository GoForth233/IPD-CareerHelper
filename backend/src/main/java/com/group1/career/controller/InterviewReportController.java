package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Interview Report API", description = "Post-interview review report generation")
@RestController
@RequestMapping("/api/interviews/report")
@RequiredArgsConstructor
public class InterviewReportController {

    private final InterviewService interviewService;

    @Operation(summary = "Generate radar chart data and report for a completed interview")
    @GetMapping("/{interviewId}")
    public Result<InterviewReportDto> generateReport(@PathVariable Long interviewId) {
        Interview interview = interviewService.getInterviewById(interviewId);
        List<InterviewMessage> messages = interviewService.getInterviewMessages(interviewId);

        // Calculate dimension scores based on conversation analysis
        int totalMessages = (int) messages.stream().filter(m -> "USER".equals(m.getRole())).count();

        // Simulated multi-dimension scoring (in production, AI would evaluate each dimension)
        int expressionScore = Math.min(95, 60 + totalMessages * 3);
        int logicScore = Math.min(90, 55 + totalMessages * 4);
        int technicalScore = Math.min(85, 50 + totalMessages * 2);
        int pressureScore = Math.min(88, 65 + totalMessages * 2);
        int communicationScore = Math.min(92, 58 + totalMessages * 3);

        RadarChartData radarData = RadarChartData.builder()
                .expression(expressionScore)
                .logic(logicScore)
                .technical(technicalScore)
                .pressureResistance(pressureScore)
                .communication(communicationScore)
                .build();

        int overallScore = (expressionScore + logicScore + technicalScore + pressureScore + communicationScore) / 5;

        // Generate text summary
        String summary = generateSummary(interview.getPositionName(), overallScore, radarData);

        InterviewReportDto report = InterviewReportDto.builder()
                .interviewId(interviewId)
                .positionName(interview.getPositionName())
                .difficulty(interview.getDifficulty())
                .overallScore(overallScore)
                .totalQuestions(totalMessages)
                .radarChart(radarData)
                .textSummary(summary)
                .build();

        return Result.success(report);
    }

    private String generateSummary(String position, int score, RadarChartData radar) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Interview for %s position completed. Overall score: %d/100.\n\n", position, score));

        // Find strongest and weakest dimensions
        int max = Math.max(Math.max(radar.getExpression(), radar.getLogic()),
                Math.max(radar.getTechnical(), Math.max(radar.getPressureResistance(), radar.getCommunication())));
        int min = Math.min(Math.min(radar.getExpression(), radar.getLogic()),
                Math.min(radar.getTechnical(), Math.min(radar.getPressureResistance(), radar.getCommunication())));

        if (max == radar.getExpression()) sb.append("Strength: Excellent verbal expression ability.\n");
        else if (max == radar.getLogic()) sb.append("Strength: Strong logical thinking skills.\n");
        else if (max == radar.getTechnical()) sb.append("Strength: Solid technical knowledge base.\n");
        else if (max == radar.getCommunication()) sb.append("Strength: Outstanding communication skills.\n");

        if (min == radar.getTechnical()) sb.append("Area for improvement: Deepen technical fundamentals.\n");
        else if (min == radar.getPressureResistance()) sb.append("Area for improvement: Build resilience under pressure.\n");
        else if (min == radar.getLogic()) sb.append("Area for improvement: Practice structured problem-solving.\n");

        return sb.toString();
    }

    // ================= DTO Classes =================

    @Data
    @Builder
    @AllArgsConstructor
    public static class InterviewReportDto {
        private Long interviewId;
        private String positionName;
        private String difficulty;
        private int overallScore;
        private int totalQuestions;
        private RadarChartData radarChart;
        private String textSummary;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RadarChartData {
        private int expression;        // 表达能力
        private int logic;             // 逻辑思维
        private int technical;         // 技术深度
        private int pressureResistance;// 抗压能力
        private int communication;     // 沟通协作
    }
}
