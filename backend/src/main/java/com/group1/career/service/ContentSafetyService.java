package com.group1.career.service;

/**
 * F29: Lightweight content safety gate.
 *
 * <p>Applied before any user-generated content (UGC question submissions,
 * feedback, AI-generated content) is persisted. Uses a local sensitive-word
 * filter as primary defence; Aliyun content moderation API as optional
 * enhancement when {@code content-safety.aliyun-enabled} is true.</p>
 *
 * <p>The entire service can be toggled off for development via
 * {@code content-safety.enabled=false}.</p>
 */
public interface ContentSafetyService {

    /**
     * Check the given text.
     *
     * @param text text to check (not null)
     * @return result — {@code passed=true} means content is acceptable
     */
    ContentCheckResult check(String text);

    /** @return false when the service is disabled (dev mode). */
    boolean isEnabled();

    /** Immutable result of a content safety check. */
    record ContentCheckResult(boolean passed, String reason) {
        public static ContentCheckResult ok() {
            return new ContentCheckResult(true, null);
        }
        public static ContentCheckResult blocked(String reason) {
            return new ContentCheckResult(false, reason);
        }
    }
}
