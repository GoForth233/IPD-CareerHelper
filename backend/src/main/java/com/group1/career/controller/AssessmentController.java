package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.*;
import com.group1.career.repository.AssessmentOptionRepository;
import com.group1.career.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Assessment API", description = "Endpoints for MBTI/Holland vocational quiz banks")
@RestController
@RequestMapping("/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final AssessmentOptionRepository optionRepository;

    @Operation(summary = "Get all assessment scales (test types)")
    @GetMapping("/scales")
    public Result<List<AssessmentScale>> getScales() {
        return Result.success(assessmentService.getAllScales());
    }

    @Operation(summary = "Get questions with options for a scale")
    @GetMapping("/scales/{scaleId}/questions")
    public Result<List<QuizQuestionDto>> getQuestions(@PathVariable Long scaleId) {
        List<AssessmentQuestion> questions = assessmentService.getScaleQuestions(scaleId);

        List<Long> questionIds = questions.stream()
                .map(AssessmentQuestion::getQuestionId)
                .collect(Collectors.toList());

        Map<Long, List<AssessmentOption>> optionsByQuestion = optionRepository
                .findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId));

        List<QuizQuestionDto> result = questions.stream().map(q -> {
            QuizQuestionDto dto = new QuizQuestionDto();
            dto.setQuestionId(q.getQuestionId());
            dto.setQuestionText(q.getQuestionText());
            dto.setQuestionType(q.getQuestionType());
            dto.setDimensionCode(q.getDimensionCode());
            dto.setSortOrder(q.getSortOrder());
            dto.setOptions(optionsByQuestion.getOrDefault(q.getQuestionId(), List.of())
                    .stream().map(o -> {
                        OptionDto od = new OptionDto();
                        od.setOptionId(o.getOptionId());
                        od.setOptionLabel(o.getOptionLabel());
                        od.setOptionText(o.getOptionText());
                        return od;
                    }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Operation(summary = "Submit answers and get result")
    @PostMapping("/submit")
    public Result<AssessmentRecord> submit(@RequestBody SubmitDto request) {
        AssessmentRecord record = assessmentService.submitAndScore(
                request.getUserId(),
                request.getScaleId(),
                request.getAnswers()
        );
        return Result.success(record);
    }

    @Operation(summary = "Get user's assessment history")
    @GetMapping("/records/{userId}")
    public Result<List<AssessmentRecord>> getUserRecords(@PathVariable Long userId) {
        return Result.success(assessmentService.getUserRecords(userId));
    }

    @Operation(summary = "Get a specific assessment record")
    @GetMapping("/records/detail/{recordId}")
    public Result<AssessmentRecord> getRecord(@PathVariable Long recordId) {
        return Result.success(assessmentService.getRecord(recordId));
    }

    // ================= DTO Classes =================

    @Data
    public static class QuizQuestionDto {
        private Long questionId;
        private String questionText;
        private String questionType;
        private String dimensionCode;
        private Integer sortOrder;
        private List<OptionDto> options;
    }

    @Data
    public static class OptionDto {
        private Long optionId;
        private String optionLabel;
        private String optionText;
    }

    @Data
    public static class SubmitDto {
        private Long userId;
        private Long scaleId;
        private Map<Long, Long> answers; // questionId -> optionId
    }
}
