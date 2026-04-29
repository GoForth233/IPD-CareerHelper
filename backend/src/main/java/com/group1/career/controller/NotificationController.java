package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.Notification;
import com.group1.career.service.NotificationService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Notification API", description = "In-app notifications surfaced on Messages > System")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "List the current user's notifications, newest first")
    @GetMapping
    public Result<List<Notification>> list() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(notificationService.listForUser(uid));
    }

    @Operation(summary = "Unread count for the current user")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(Map.of("count", notificationService.unreadCount(uid)));
    }

    @Operation(summary = "Mark a single notification as read")
    @PostMapping("/{notificationId}/read")
    public Result<String> markRead(@PathVariable Long notificationId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        notificationService.markRead(notificationId, uid);
        return Result.success("ok");
    }

    @Operation(summary = "Mark every notification as read for the current user")
    @PostMapping("/read-all")
    public Result<Map<String, Integer>> markAllRead() {
        Long uid = SecurityUtil.requireCurrentUserId();
        int updated = notificationService.markAllRead(uid);
        return Result.success(Map.of("updated", updated));
    }
}
