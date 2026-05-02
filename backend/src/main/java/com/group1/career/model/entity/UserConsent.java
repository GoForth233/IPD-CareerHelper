package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * F2: Server-side record of a user's consent to a specific agreement version.
 *
 * <p>One row per (user_id, agreement_version). When a new policy version is
 * released, a new row is inserted rather than updating the old one, preserving
 * the full audit trail for WeChat and app store review purposes.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_consents",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_consent_version",
                columnNames = {"user_id", "agreement_version"}),
        indexes = @Index(name = "idx_user_consents_user", columnList = "user_id"))
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Semantic version string matching the frontend constant, e.g. "1.0". */
    @Column(name = "agreement_version", nullable = false, length = 20)
    private String agreementVersion;

    @Column(name = "agreed_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime agreedAt;

    /** Best-effort client IP forwarded by the frontend. */
    @Column(name = "client_ip", length = 64)
    private String clientIp;

    /** "miniprogram" | "h5" | "app" */
    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
