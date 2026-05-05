package com.group1.career.service;

import com.group1.career.model.dto.AgentBundleDto;
import com.group1.career.model.dto.CareerAgentTodayDto;
import com.group1.career.model.dto.CareerAgentRiskDto;
import com.group1.career.model.dto.CareerAgentPlanDto;
import com.group1.career.model.entity.AgentTask;

import java.util.List;

public interface CareerAgentService {
    AgentBundleDto getBundle(Long userId);
    CareerAgentTodayDto getToday(Long userId);
    CareerAgentRiskDto getRiskWatch(Long userId);
    CareerAgentPlanDto getPlanSummary(Long userId);
    CareerAgentPlanDto ensurePlan(Long userId);
    List<AgentTask> getTodayTasks(Long userId);
    List<AgentTask> getOpenTasks(Long userId);
    AgentTask completeTask(Long userId, Long taskId);
    AgentTask dismissTask(Long userId, Long taskId);
}
