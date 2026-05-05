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
 * Phase E3: structured event log for user–agent interactions.
 *
 * <p>Event types: TASK_COMPLETED, TASK_DISMISSED, RISK_CHANGED,
 * PLAN_GENERATED, RESUME_SCORE_CHANGED, INTERVIEW_SCORE_CHANGED.
 * The {@code event_payload} JSON column holds event-specific details.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_events", indexes = {
        @Index(name = "idx_agent_events_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_agent_events_user_type_created", columnList = "user_id, event_type, created_at")
})
public class AgentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** TASK_COMPLETED | TASK_DISMISSED | RISK_CHANGED | PLAN_GENERATED
     *  | RESUME_SCORE_CHANGED | INTERVIEW_SCORE_CHANGED */
    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "event_payload", columnDefinition = "json")
    private String eventPayload;

    /** USER | SYSTEM | SCHEDULER */
    @Column(name = "source", nullable = false, length = 40)
    @Builder.Default
    private String source = "SYSTEM";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
