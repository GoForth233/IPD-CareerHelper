package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * F28c: AI-generated personalised career plan.
 *
 * <p>One row per user. The plan is generated on-demand (or regenerated when
 * the user's profile snapshot changes significantly) by the career plan
 * service using the user's profile_snapshot + user_facts as input. The JSON
 * columns store the LLM's structured output so the frontend can render it
 * without re-calling the AI.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_career_plans",
        uniqueConstraints = @UniqueConstraint(name = "uk_career_plan_user", columnNames = "user_id"))
public class UserCareerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_role", length = 200)
    private String targetRole;

    /** JSON snapshot of the user's current skill / experience state when plan was generated. */
    @Column(name = "start_state_json", columnDefinition = "json")
    private String startStateJson;

    /** JSON array of career milestones, each with title + target_date + criteria. */
    @Column(name = "milestones_json", columnDefinition = "json")
    private String milestonesJson;

    /** JSON array of weekly focus items for the upcoming 4 weeks. */
    @Column(name = "weekly_focus_json", columnDefinition = "json")
    private String weeklyFocusJson;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(name = "tokens_consumed", nullable = false)
    @Builder.Default
    private Integer tokensConsumed = 0;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;

    @Column(name = "last_updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;
}
