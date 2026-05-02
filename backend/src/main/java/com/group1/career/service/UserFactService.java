package com.group1.career.service;

import com.group1.career.model.entity.UserFact;

import java.util.List;

/**
 * F12: AI-extracted user fact memory.
 *
 * <p>After each conversation the assistant asynchronously scans the recent
 * messages with qwen-turbo and extracts structured facts (MBTI type, career
 * goals, skills …). Facts are upserted by {@code fact_key} so the same fact
 * is never duplicated. Users can review and delete facts from the memory
 * management page in the app.</p>
 */
public interface UserFactService {

    /** Return all facts for the given user. */
    List<UserFact> getUserFacts(Long userId);

    /**
     * Async: extract structured facts from recent session messages and upsert
     * them into {@code user_facts}. Fire-and-forget; callers do not block.
     *
     * @param userId    owner
     * @param sessionId session to mine for new facts
     */
    void extractAndSaveAsync(Long userId, Long sessionId);

    /**
     * Delete a single fact. Returns false if the fact does not belong to
     * {@code userId} (ownership guard).
     */
    boolean deleteFact(Long userId, Long factId);

    /**
     * Render all facts as a compact prompt snippet for injection into the
     * system message. Returns an empty string when the user has no facts.
     */
    String renderForPrompt(Long userId);
}
