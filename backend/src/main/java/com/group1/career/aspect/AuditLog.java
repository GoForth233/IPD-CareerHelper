package com.group1.career.aspect;

import java.lang.annotation.*;

/**
 * F19: Marks an admin controller method whose execution should be recorded
 * in {@code admin_audit_log}. The {@link AdminAuditAspect} intercepts
 * any method carrying this annotation and saves the log entry after the
 * method returns successfully.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** Short action verb written to the log, e.g. "BAN_USER". */
    String action();

    /** Entity category, e.g. "USER", "QUESTION", "ARTICLE". */
    String targetType();

    /**
     * Zero-based index of the method parameter that contains the target
     * entity's ID. Set to {@code -1} when there is no meaningful ID
     * parameter (e.g. broadcast-to-all calls).
     */
    int idParamIndex() default 0;
}
