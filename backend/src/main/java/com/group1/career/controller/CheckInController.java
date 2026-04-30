package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.CheckIn;
import com.group1.career.repository.CheckInRepository;
import com.group1.career.service.CheckInService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 7-day check-in endpoints — driven by SecurityUtil/JWT. The frontend
 * usually doesn't need to call /trigger because each core domain (assessment
 * submit, interview end, skill node complete) triggers a recordAction
 * server-side. We expose /trigger for unit testing and admin recovery.
 */
@Tag(name = "Check-in API", description = "7-day check-in / streak / weekly badge")
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final CheckInRepository repo;

    @Operation(summary = "Get the current user's check-in status (today + weekly + streak)")
    @GetMapping("/status")
    public Result<CheckInService.CheckInStatus> status() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(checkInService.getStatus(uid));
    }

    @Operation(summary = "Manually trigger a check-in (debug/recovery only — normal flows trigger server-side)")
    @PostMapping("/trigger")
    public Result<CheckInService.CheckInStatus> trigger(@RequestParam String action) {
        Long uid = SecurityUtil.requireCurrentUserId();
        checkInService.recordAction(uid, action);
        return Result.success(checkInService.getStatus(uid));
    }

    @Operation(summary = "Get raw check-in rows in the last 30 days (calendar view backing data)")
    @GetMapping("/calendar")
    public Result<List<CheckIn>> calendar() {
        Long uid = SecurityUtil.requireCurrentUserId();
        LocalDate today = LocalDate.now();
        return Result.success(repo.findByUserIdAndDayBetweenOrderByDayAsc(uid, today.minusDays(29), today));
    }
}
