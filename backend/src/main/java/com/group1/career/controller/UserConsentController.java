package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.UserConsent;
import com.group1.career.repository.UserConsentRepository;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * F2: Records user consent server-side for audit / WeChat review purposes.
 *
 * <p>The frontend calls POST /api/consents immediately after the user ticks
 * both checkboxes and taps "Agree". The endpoint is intentionally lenient:
 * it accepts consent from both authenticated and unauthenticated users (the
 * user may not have logged in yet when they accept the terms on first launch).
 * A follow-up upsert on login can reconcile the record if needed.</p>
 */
@Tag(name = "Consent API", description = "F2: Record and verify user agreement to privacy policy and terms")
@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
public class UserConsentController {

    /** Must match the AGREEMENT_VERSION constant in the frontend consent page. */
    public static final String CURRENT_VERSION = "1.0";

    private final UserConsentRepository consentRepository;

    @Operation(summary = "Record that the current user has agreed to the specified agreement version")
    @PostMapping
    public Result<String> recordConsent(@RequestBody ConsentRequest req,
                                        HttpServletRequest httpRequest) {
        Long uid = SecurityUtil.currentUserId();
        if (uid == null) {
            return Result.error(401, "Not authenticated");
        }

        String version = req.getAgreementVersion() != null
                ? req.getAgreementVersion() : CURRENT_VERSION;

        if (consentRepository.existsByUserIdAndAgreementVersion(uid, version)) {
            return Result.success("already_recorded");
        }

        String ip = extractIp(httpRequest);

        consentRepository.save(UserConsent.builder()
                .userId(uid)
                .agreementVersion(version)
                .agreedAt(LocalDateTime.now())
                .clientIp(ip)
                .platform(req.getPlatform())
                .userAgent(req.getUserAgent())
                .build());

        return Result.success("recorded");
    }

    @Operation(summary = "Check whether the current user has agreed to the current agreement version")
    @GetMapping("/me/status")
    public Result<ConsentStatusDto> getStatus() {
        Long uid = SecurityUtil.currentUserId();
        if (uid == null) return Result.error(401, "Not authenticated");

        boolean agreed = consentRepository.existsByUserIdAndAgreementVersion(uid, CURRENT_VERSION);
        ConsentStatusDto dto = new ConsentStatusDto();
        dto.setAgreed(agreed);
        dto.setCurrentVersion(CURRENT_VERSION);
        return Result.success(dto);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private String extractIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    // ── DTOs ─────────────────────────────────────────────────────────────

    @Data
    public static class ConsentRequest {
        private String agreementVersion;
        private String platform;
        private String userAgent;
    }

    @Data
    public static class ConsentStatusDto {
        private boolean agreed;
        private String currentVersion;
    }
}
