package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.UserFeedback;
import com.group1.career.repository.UserFeedbackRepository;
import com.group1.career.service.AdminAuthService;
import com.group1.career.service.EmailService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * F26: User feedback / complaint channel.
 *
 * <p>Public endpoint POST /api/feedback/submit — no auth required so users
 * who are locked out can still reach support. Admin endpoints sit under
 * /api/admin/feedback and are guarded by the admin role check.</p>
 */
@Slf4j
@Tag(name = "Feedback API", description = "F26 — user feedback submission and admin management")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final UserFeedbackRepository feedbackRepository;
    private final EmailService emailService;
    private final AdminAuthService adminAuthService;

    // ─────────────────────────────────────────────
    // Public endpoint — no auth required
    // ─────────────────────────────────────────────

    @Operation(summary = "Submit user feedback (auth optional — anonymous submissions allowed)")
    @PostMapping("/api/feedback/submit")
    public Result<Long> submit(@RequestBody SubmitRequest req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new BizException("反馈内容不能为空");
        }
        if (req.getContent().length() > 2000) {
            throw new BizException("反馈内容不超过 2000 字");
        }
        String category = resolveCategory(req.getCategory());

        UserFeedback fb = UserFeedback.builder()
                .userId(SecurityUtil.currentUserId())
                .category(category)
                .content(req.getContent().trim())
                .contact(req.getContact())
                .attachmentUrls(req.getAttachmentUrls())
                .status("PENDING")
                .build();
        fb = feedbackRepository.save(fb);

        Long uid = SecurityUtil.currentUserId();
        emailService.sendFeedbackAlert(fb.getId(), category,
                uid != null ? uid.toString() : null,
                fb.getContent(), fb.getContact());

        log.info("[feedback] submitted id={} category={} uid={}", fb.getId(), category, uid);
        return Result.success(fb.getId());
    }

    // ─────────────────────────────────────────────
    // Admin endpoints
    // ─────────────────────────────────────────────

    @Operation(summary = "List feedbacks (admin) — paginated, optionally filtered by status")
    @GetMapping("/api/admin/feedback")
    public Result<Page<UserFeedback>> listFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        requireAdmin();
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFeedback> result = (status != null && !status.isBlank())
                ? feedbackRepository.findByStatusOrderByCreatedAtDesc(status, pr)
                : feedbackRepository.findAllByOrderByCreatedAtDesc(pr);
        return Result.success(result);
    }

    @Operation(summary = "Update feedback status (admin) — PROCESSING | REPLIED | CLOSED")
    @PostMapping("/api/admin/feedback/{id}/status")
    public Result<UserFeedback> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        requireAdmin();
        UserFeedback fb = feedbackRepository.findById(id)
                .orElseThrow(() -> new BizException("Feedback not found"));
        String newStatus = body.getOrDefault("status", "").toUpperCase();
        List<String> allowed = List.of("PENDING", "PROCESSING", "REPLIED", "CLOSED");
        if (!allowed.contains(newStatus)) {
            throw new BizException("Invalid status: " + newStatus);
        }
        fb.setStatus(newStatus);
        if ("REPLIED".equals(newStatus) && fb.getRepliedAt() == null) {
            fb.setRepliedAt(LocalDateTime.now());
        }
        return Result.success(feedbackRepository.save(fb));
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private String resolveCategory(String raw) {
        if (raw == null || raw.isBlank()) return "OTHER";
        return switch (raw.toUpperCase()) {
            case "FUNCTION_BUG"     -> "FUNCTION_BUG";
            case "SUGGESTION"       -> "SUGGESTION";
            case "CONTENT_REPORT"   -> "CONTENT_REPORT";
            default                 -> "OTHER";
        };
    }

    private void requireAdmin() {
        Long uid = SecurityUtil.requireCurrentUserId();
        adminAuthService.requireAdmin(uid);
    }

    // ─────────────────────────────────────────────
    // DTO
    // ─────────────────────────────────────────────

    @lombok.Data
    public static class SubmitRequest {
        /** FUNCTION_BUG | SUGGESTION | CONTENT_REPORT | OTHER */
        private String category;
        private String content;
        private String contact;
        /** JSON array of OSS keys, e.g. ["feedback/uuid1.jpg"] */
        private String attachmentUrls;
    }
}
