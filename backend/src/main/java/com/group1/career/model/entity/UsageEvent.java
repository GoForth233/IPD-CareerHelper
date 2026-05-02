package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * F18: Lightweight usage event tracking.
 *
 * <p>Append-only table. Each interesting user action fires a single INSERT
 * here. The admin analytics dashboard aggregates these with COUNT / GROUP BY
 * queries, rendered as ECharts on the admin frontend.</p>
 *
 * <p>event_type examples: INTERVIEW_STARTED, INTERVIEW_COMPLETED,
 * ASSESSMENT_SUBMITTED, RESUME_UPLOADED, AI_CHAT_TURN, PAGE_VIEW_HOME</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usage_events", indexes = {
        @Index(name = "idx_usage_events_user_type", columnList = "user_id, event_type"),
        @Index(name = "idx_usage_events_created",   columnList = "created_at")
})
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Nullable — anonymous / pre-login events are allowed. */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_type", length = 60, nullable = false)
    private String eventType;

    /** Optional JSON metadata, e.g. {\"position\":\"Java\",\"difficulty\":\"Hard\"}. */
    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
