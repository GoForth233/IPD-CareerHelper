package com.group1.career.service;

import com.group1.career.model.entity.AgentEvent;
import com.group1.career.model.entity.AgentState;

import java.util.Optional;

public interface AgentStateService {

    /** Return the current state row, creating an empty one if absent. */
    AgentState getOrCreate(Long userId);

    /**
     * Recompute task completion / dismiss rates, stage, primary risk, and
     * preferred difficulty from live data, then upsert the state row.
     * Called at the end of every {@code getToday} invocation.
     */
    AgentState refresh(Long userId, String currentStage, String primaryRiskCode,
                       String primaryGoal, String preferredDifficulty);

    /** Latest WEEKLY_REVIEW_COMPLETED event for this user, if any. */
    Optional<AgentEvent> getLatestWeeklyReview(Long userId);
}
