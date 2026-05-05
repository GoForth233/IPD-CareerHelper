package com.group1.career.model.dto;

import lombok.Data;

/**
 * Phase E2: user-supplied inputs that supplement the AI-inferred profile.
 *
 * <p>All fields are optional — the frontend only sends what the user filled in.
 * Each non-null field is upserted into {@code user_facts} with
 * {@code source = USER_INPUT, confidence = 1.00}, then the unified
 * profile is force-rebuilt so changes are immediately visible.</p>
 */
@Data
public class ProfileInputsRequest {

    /** Target city/region, e.g. "上海", "北京". */
    private String targetCity;

    /** Target industry, e.g. "互联网", "金融". */
    private String targetIndustry;

    /** Job-search timeline, e.g. "3个月", "6个月", "校招季". */
    private String timeline;

    /** Hours per week the user can invest, e.g. "5", "10", "20". */
    private String weeklyHours;

    /** Preferred task difficulty: EASY | MEDIUM | HARD. */
    private String preferredDifficulty;

    /** Is the user considering graduate school? true / false. */
    private Boolean considerGradSchool;

    /** Is the user considering studying abroad? true / false. */
    private Boolean considerStudyAbroad;

    /** Free-text career goal supplement. */
    private String careerGoalNote;
}
