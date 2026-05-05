package com.group1.career.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerAgentPlanDto {
    private Boolean hasPlan;
    private String targetRole;
    private String planHealth;
    private String adjustmentReason;
    private String nextMilestoneHorizon;
    private String nextMilestoneTitle;
    private List<String> weeklyFocus;
    private String generatedAt;
    private String lastUpdatedAt;
    private Integer version;
}
