package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Resume Diagnosis API", description = "AI-powered resume analysis and scoring")
@RestController
@RequestMapping("/api/resume-diagnosis")
@RequiredArgsConstructor
public class ResumeDiagnosisController {

    private final AiService aiService;

    @Operation(summary = "Trigger AI resume diagnosis")
    @PostMapping("/analyze")
    public Result<DiagnosisResultDto> triggerDiagnosis(@RequestBody DiagnosisRequestDto request) {

        // Build the prompt combining resume content with the target JD
        String prompt = buildDiagnosisPrompt(request.getResumeText(), request.getJobDescription());

        // Call AI service for analysis (synchronous for now, can be async later)
        String aiResponse = aiService.chat(prompt);

        DiagnosisResultDto result = new DiagnosisResultDto();
        result.setOverallScore(calculateScore(aiResponse));
        result.setAnalysis(aiResponse);
        result.setResumeId(request.getResumeId());

        return Result.success(result);
    }

    private String buildDiagnosisPrompt(String resumeText, String jd) {
        return "You are a professional HR resume reviewer. " +
               "Please analyze the following resume against the job description and provide:\n" +
               "1. Overall match score (0-100)\n" +
               "2. Strengths (bullet points)\n" +
               "3. Weaknesses (bullet points)\n" +
               "4. Improvement suggestions\n\n" +
               "=== Job Description ===\n" + jd + "\n\n" +
               "=== Resume Content ===\n" + resumeText;
    }

    private int calculateScore(String aiResponse) {
        // Extract score from AI response, fallback to 75
        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.contains("score") || line.contains("Score")) {
                    String num = line.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) {
                        int score = Integer.parseInt(num.substring(0, Math.min(num.length(), 3)));
                        return Math.min(score, 100);
                    }
                }
            }
        } catch (Exception ignored) {}
        return 75;
    }

    // ================= DTO Classes =================

    @Data
    public static class DiagnosisRequestDto {
        private Long resumeId;
        private String resumeText;
        private String jobDescription;
    }

    @Data
    public static class DiagnosisResultDto {
        private Long resumeId;
        private int overallScore;
        private String analysis;
    }
}
