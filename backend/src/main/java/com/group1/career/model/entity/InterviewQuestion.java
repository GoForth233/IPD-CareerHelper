package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Crowd-sourced "interview market" question.
 *
 * <p>A question lives independently of the interview it came from. We store
 * a SHA-256 hash of the contributor's userId rather than the userId itself so
 * the question bank is anonymous to the public list endpoint, but we can
 * still rate-limit and detect spam from a single contributor without joining
 * back to the users table.</p>
 *
 * <p>The (position, difficulty) tuple is the primary index — both the
 * /api/questions list endpoint and the InterviewController greeting "draw"
 * filter on these. {@code likes} is bumped lazily; we don't bother with a
 * separate vote table because the requirement is a soft popularity signal,
 * not authoritative voting.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interview_questions", indexes = {
        @Index(name = "idx_iq_pos_diff", columnList = "position, difficulty"),
        @Index(name = "idx_iq_likes", columnList = "likes"),
        @Index(name = "idx_iq_created", columnList = "created_at")
})
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Position name as typed by the candidate (e.g. "Frontend Engineer"). */
    @Column(name = "position", length = 80, nullable = false)
    private String position;

    /** Easy / Normal / Hard. */
    @Column(name = "difficulty", length = 16, nullable = false)
    private String difficulty;

    /** Free-form question text — what we feed to the AI as the angle. */
    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    /** Optional one-liner so the market list reads nicely in 1 line. */
    @Column(name = "summary", length = 200)
    private String summary;

    /**
     * SHA-256 of the contributor's userId. We never store the raw id so
     * the public list endpoint cannot be reversed into a user enumeration.
     */
    @Column(name = "contributor_hash", length = 64)
    private String contributorHash;

    /** Cached like count; updated by /like. */
    @Column(name = "likes", nullable = false)
    @Builder.Default
    private Integer likes = 0;

    /** Lifetime times this question was sampled into a real interview. */
    @Column(name = "draw_count", nullable = false)
    @Builder.Default
    private Integer drawCount = 0;

    /** APPROVED (default) | HIDDEN — keeps stub for future moderation. */
    @Column(name = "status", length = 16, nullable = false)
    @Builder.Default
    private String status = "APPROVED";

    /**
     * F28a three-tier source:
     * <ul>
     *   <li>OFFICIAL — admin-seeded canonical questions (pre-loaded in V3 seed SQL)</li>
     *   <li>USER — crowd-sourced via /api/questions (default)</li>
     *   <li>AI_GENERATED — produced by F13 function-calling</li>
     * </ul>
     */
    @Column(name = "source", length = 20, nullable = false)
    @Builder.Default
    private String source = "USER";

    /**
     * F28a review workflow:
     * PUBLISHED — visible in the public list (default for USER/OFFICIAL)<br>
     * PENDING_REVIEW — AI_GENERATED awaits admin moderation<br>
     * REJECTED — hidden by admin
     */
    @Column(name = "review_status", length = 20, nullable = false)
    @Builder.Default
    private String reviewStatus = "PUBLISHED";

    /** Reference answer in Markdown. Set on OFFICIAL and AI_GENERATED questions. */
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
