package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.UserFact;
import com.group1.career.service.UserFactService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * F12: User memory (extracted facts) management API.
 */
@Tag(name = "User Memory API", description = "F12: AI-extracted user facts — list and delete")
@RestController
@RequestMapping("/api/facts")
@RequiredArgsConstructor
public class UserFactController {

    private final UserFactService userFactService;

    @Operation(summary = "List all AI-extracted facts for the current user")
    @GetMapping("/me")
    public Result<List<UserFact>> getMyFacts() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(userFactService.getUserFacts(uid));
    }

    @Operation(summary = "Delete a single fact (owner-only)")
    @DeleteMapping("/me/{factId}")
    public Result<String> deleteFact(@PathVariable Long factId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        boolean deleted = userFactService.deleteFact(uid, factId);
        return deleted ? Result.success("Deleted") : Result.error(403, "Fact not found or access denied");
    }
}
