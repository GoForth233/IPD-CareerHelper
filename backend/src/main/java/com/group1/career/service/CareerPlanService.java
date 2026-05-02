package com.group1.career.service;

import com.group1.career.model.entity.UserCareerPlan;

import java.util.Optional;

/**
 * F28c: AI-generated personalised career plan.
 *
 * <p>One plan per user. Regenerated on-demand or when the user's profile
 * changes (new assessment result, new resume upload). Weekly focus items
 * are refreshed every Sunday at 19:00 by CareerPlanWeeklyJob.</p>
 */
public interface CareerPlanService {

    /**
     * Generate (or regenerate) the full AI career plan for the user.
     * Blocks until generation is complete. If AI fails, falls back to a
     * sensible default plan so the UI never shows an empty state.
     *
     * @param userId     target user
     * @param targetRole explicit target role supplied by the user (nullable —
     *                   AI infers from profile when absent)
     * @return saved plan (never null)
     */
    UserCareerPlan generate(Long userId, String targetRole);

    /**
     * Return the latest saved plan for the user, or empty if none exists yet.
     */
    Optional<UserCareerPlan> getCurrent(Long userId);

    /**
     * Asynchronously regenerate the plan (called after assessment / resume
     * changes so the plan stays current without blocking the caller).
     */
    void regenerateAsync(Long userId);
}
