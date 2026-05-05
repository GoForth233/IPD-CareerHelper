package com.group1.career.service;

import com.group1.career.model.dto.AgentUserProfileDto;
import com.group1.career.model.dto.ProfileInputsRequest;

public interface AgentProfileService {

    /** Read cached profile; rebuilds and saves if missing. */
    AgentUserProfileDto getProfile(Long userId);

    /** Force-rebuild from all data sources and persist. */
    AgentUserProfileDto refresh(Long userId);

    /** Upsert user-supplied inputs into user_facts and rebuild profile. */
    AgentUserProfileDto saveInputs(Long userId, ProfileInputsRequest req);

    /**
     * E7: Render the unified career profile as a compact, LLM-ready context
     * block. Returns an empty string when no profile exists yet (callers should
     * fall back to {@link com.group1.career.service.UserProfileSnapshotService#renderForPrompt}).
     */
    String renderForPrompt(Long userId);
}
