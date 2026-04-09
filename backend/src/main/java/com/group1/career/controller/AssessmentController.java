package com.group1.career.controller;

import com.group1.career.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Assessment API", description = "Endpoints for MBTI/Holland vocational quiz banks")
@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    @Operation(summary = "Get Quiz Questions")
    @GetMapping("/questions")
    public Result<List<QuizQuestionDto>> getQuestions(@RequestParam(defaultValue = "MBTI") String type) {
        
        // Simulating loading questions from the Assessment Document Database
        List<QuizQuestionDto> questions;
        
        if ("MBTI".equalsIgnoreCase(type)) {
            questions = Arrays.asList(
                new QuizQuestionDto(1L, "At a party do you:", "A: Interact with many, including strangers", "B: Interact with a few, known to you"),
                new QuizQuestionDto(2L, "Are you more:", "A: Realistic than speculative", "B: Speculative than realistic"),
                new QuizQuestionDto(3L, "Is it worse to:", "A: Have your head in the clouds", "B: Be in a rut")
                // In production, this would return 120 questions parsed from MongoDB
            );
        } else {
            questions = Arrays.asList(
                new QuizQuestionDto(101L, "I like to work on cars", "A: True", "B: False"),
                new QuizQuestionDto(102L, "I like to work with data and numbers", "A: True", "B: False")
            );
        }

        return Result.success(questions);
    }

    // ================= DTO Classes =================

    @Data
    @AllArgsConstructor
    public static class QuizQuestionDto {
        private Long id;
        private String prompt;
        private String optionA;
        private String optionB;
    }
}
