package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * F25: Minimal account deletion audit log.
 *
 * <p>Records the fact of deletion without storing PII. Kept for regulatory
 * compliance (证明用户确实申请过注销) and abuse detection
 * (re-registration from the same IP within 24h).</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account_deletion_log")
public class AccountDeletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** SHA-256 of the remote IP address. Not the raw IP — no PII stored here. */
    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
