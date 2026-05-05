package com.group1.career.service;

/**
 * Generates a short, personalised one-sentence "reason" for the Career Agent
 * today card using qwen-turbo, with Redis caching and rule-based fallback.
 */
public interface AgentReasonService {

    /**
     * Returns a personalised reason string for the given user and stage.
     * Tries Redis cache first; calls qwen-turbo if not cached; falls back
     * to {@code fallback} on any error or timeout.
     *
     * @param userId   authenticated user ID
     * @param stage    current career stage key (e.g. RESUME_IMPROVEMENT)
     * @param context  brief context summary used as LLM prompt (Chinese)
     * @param fallback rule-based reason to return if LLM/cache unavailable
     */
    String reason(Long userId, String stage, String context, String fallback);
}
