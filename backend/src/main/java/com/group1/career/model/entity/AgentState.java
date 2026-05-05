package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phase E4: persistent agent state — one row per user.
 *
 * <p>Updated after every {@code getToday} call and by the weekly review job.
 * Acts as a lightweight cache so the Agent Hub page can load fast without
 * re-running every scoring rule on every request.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_states")
public class AgentState {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "current_stage", length = 50)
    private String currentStage;

    @Column(name = "primary_goal", length = 200)
    private String primaryGoal;

    @Column(name = "primary_risk_code", length = 50)
    private String primaryRiskCode;

    @Column(name = "task_completion_rate_7d", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taskCompletionRate7d = BigDecimal.ZERO;

    @Column(name = "task_dismiss_rate_7d", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taskDismissRate7d = BigDecimal.ZERO;

    @Column(name = "preferred_task_difficulty", nullable = false, length = 20)
    @Builder.Default
    private String preferredTaskDifficulty = "MEDIUM";

    @Column(name = "last_active_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;

    @Column(name = "last_plan_adjusted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPlanAdjustedAt;

    @Column(name = "last_weekly_review_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastWeeklyReviewAt;

    @Column(name = "state_json", columnDefinition = "json")
    private String stateJson;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
