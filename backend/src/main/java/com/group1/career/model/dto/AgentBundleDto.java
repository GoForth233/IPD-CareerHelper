package com.group1.career.model.dto;

import com.group1.career.model.entity.AgentTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentBundleDto {
    private CareerAgentTodayDto today;
    private List<AgentTask> tasks;
    private CareerAgentRiskDto risk;
    private CareerAgentPlanDto plan;
    private AgentUserProfileDto profile;
}
