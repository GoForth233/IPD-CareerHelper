package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * F12: AI-extracted user fact memory.
 *
 * <p>After each assistant session the AI service extracts structured facts
 * (MBTI type, target role, skill gaps …) and upserts them here keyed by
 * {@code fact_key}. On the next turn the facts are appended to the system
 * prompt so the AI doesn't have to re-discover them.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_facts",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_fact_key", columnNames = {"user_id", "fact_key"}),
        indexes = @Index(name = "idx_user_facts_user", columnList = "user_id"))
public class UserFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** PERSONALITY | CAREER_GOAL | SKILL | PREFERENCE | EXPERIENCE */
    @Column(name = "category", length = 50, nullable = false)
    private String category;

    /** Short unique key, e.g. "mbti_type", "target_role", "weak_skill_sql". */
    @Column(name = "fact_key", length = 100, nullable = false)
    private String factKey;

    /** The actual fact value, e.g. "INTJ", "Backend Engineer", "SQL queries". */
    @Column(name = "fact_value", nullable = false, columnDefinition = "TEXT")
    private String factValue;

    /** AI confidence 0.00–1.00 (1.00 = user explicitly stated it). */
    @Column(name = "confidence", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal confidence = BigDecimal.ONE;

    /** AI_EXTRACTED | USER_INPUT | ASSESSMENT | RESUME */
    @Column(name = "source", length = 50, nullable = false)
    @Builder.Default
    private String source = "AI_EXTRACTED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
