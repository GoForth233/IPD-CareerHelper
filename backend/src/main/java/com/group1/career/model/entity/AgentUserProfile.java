package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Phase E1: aggregated career profile feeding every agent decision.
 *
 * <p>One row per user. Fields marked {@code _json} hold semi-structured data
 * so we can evolve the schema without a migration each time. The unified
 * profile is rebuilt by {@code AgentProfileService} from the existing data
 * sources (profile snapshot, user facts, career plan, agent tasks, check-ins).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "agent_user_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uk_agent_user_profile_user", columnNames = "user_id"),
        indexes = {
                @Index(name = "idx_agent_user_profile_level", columnList = "personalization_level"),
                @Index(name = "idx_agent_user_profile_stage", columnList = "current_stage")
        })
public class AgentUserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** LOW | MEDIUM | HIGH — derived from completenessScore. */
    @Column(name = "personalization_level", nullable = false, length = 20)
    @Builder.Default
    private String personalizationLevel = "LOW";

    @Column(name = "completeness_score", nullable = false)
    @Builder.Default
    private Integer completenessScore = 0;

    @Column(name = "current_stage", length = 50)
    private String currentStage;

    @Column(name = "target_role", length = 200)
    private String targetRole;

    /** PREFERENCES | RESUME | INTERVIEW | USER_INPUT | INFERRED */
    @Column(name = "target_role_source", length = 50)
    private String targetRoleSource;

    @Column(name = "target_role_confidence", precision = 3, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal targetRoleConfidence = BigDecimal.ZERO;

    @Column(name = "primary_risk_code", length = 50)
    private String primaryRiskCode;

    @Column(name = "readiness_json", columnDefinition = "json")
    private String readinessJson;

    @Column(name = "skill_profile_json", columnDefinition = "json")
    private String skillProfileJson;

    @Column(name = "behavior_profile_json", columnDefinition = "json")
    private String behaviorProfileJson;

    @Column(name = "missing_signals_json", columnDefinition = "json")
    private String missingSignalsJson;

    @Column(name = "evidence_json", columnDefinition = "json")
    private String evidenceJson;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
