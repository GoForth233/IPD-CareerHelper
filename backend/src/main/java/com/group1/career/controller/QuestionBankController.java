package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.service.QuestionBankService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Crowd-sourced interview question market — list, like, contribute. Every
 * write requires JWT (so we can hash a contributor id), but the public list
 * endpoint never returns the raw user id.
 */
@Tag(name = "Question Bank API", description = "Interview market: list, like, contribute")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService service;

    @Operation(summary = "List questions in the market with optional filters and pagination")
    @GetMapping
    public Result<MarketPageResponse> list(
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<InterviewQuestion> p = service.list(position, difficulty, source, page, size);
        MarketPageResponse r = new MarketPageResponse();
        r.setItems(p.getContent());
        r.setTotal(p.getTotalElements());
        r.setPage(p.getNumber());
        r.setSize(p.getSize());
        return Result.success(r);
    }

    @Operation(summary = "Contribute a new interview question to the market")
    @PostMapping
    public Result<InterviewQuestion> contribute(@RequestBody ContributeRequest req) {
        Long uid = SecurityUtil.requireCurrentUserId();
        try {
            return Result.success(service.contribute(uid, req.getPosition(), req.getDifficulty(),
                    req.getContent(), req.getSummary()));
        } catch (IllegalArgumentException ex) {
            // Surface the validation copy verbatim — it's already user-friendly.
            throw new BizException(ex.getMessage());
        }
    }

    @Operation(summary = "Like a question (anonymous, no per-user vote tracking)")
    @PostMapping("/{id}/like")
    public Result<InterviewQuestion> like(@PathVariable Long id) {
        SecurityUtil.requireCurrentUserId();
        return service.like(id)
                .map(Result::success)
                .orElseThrow(() -> new BizException("Question not found"));
    }

    @Data
    public static class ContributeRequest {
        private String position;
        private String difficulty;
        private String content;
        private String summary;
    }

    @Data
    public static class MarketPageResponse {
        private List<InterviewQuestion> items;
        private long total;
        private int page;
        private int size;
    }
}
