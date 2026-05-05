package com.group1.career.service;

import com.group1.career.model.entity.AgentEvent;

import java.util.List;

public interface AgentEventService {

    /** Record any event with an arbitrary serialisable payload object. */
    void record(Long userId, String eventType, String source, Object payload);

    /**
     * Compare the incoming primary risk code against the last stored value in
     * {@code agent_user_profiles}. If it changed, write a RISK_CHANGED event
     * and update the stored code.
     */
    void recordRiskChange(Long userId, String newPrimaryCode, String newPrimaryLevel);

    /** Return the most recent {@code limit} events for a user, newest first. */
    List<AgentEvent> getRecentEvents(Long userId, int limit);
}
