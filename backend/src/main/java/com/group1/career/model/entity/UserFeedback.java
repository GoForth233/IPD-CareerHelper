package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * F26: User feedback / complaint channel.
 *
 * <p>Submitted from the mini-program's "我的 → 意见反馈" page.
 * Unauthenticated submissions are allowed (user_id nullable) so users who
 * are locked out can still reach support.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_feedback")
public class UserFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nullable — unauthenticated users may submit feedback. */
    @Column(name = "user_id")
    private Long userId;

    /** FUNCTION_BUG | SUGGESTION | CONTENT_REPORT | OTHER */
    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Optional contact email / phone for the admin to follow up. */
    @Column(name = "contact", length = 120)
    private String contact;

    /** JSON array of OSS keys for attached screenshots (max 3). */
    @Column(name = "attachment_urls", columnDefinition = "json")
    private String attachmentUrls;

    /** PENDING | PROCESSING | REPLIED | CLOSED */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "replied_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime repliedAt;
}
