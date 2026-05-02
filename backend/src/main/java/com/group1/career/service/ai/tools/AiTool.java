package com.group1.career.service.ai.tools;

import java.util.Map;

/**
 * F13: Contract for every AI function-calling tool.
 *
 * <p>Security rule: {@code execute()} receives the calling user's ID from
 * {@link com.group1.career.utils.SecurityUtil}, never from the LLM's
 * argument payload. This prevents a hallucinated {@code user_id} in the
 * model's tool arguments from accessing another user's data.</p>
 */
public interface AiTool {

    /** Unique snake_case name used in the tool schema (e.g. "get_recent_assessment"). */
    String getName();

    /** Short human-readable description shown to the LLM. */
    String getDescription();

    /**
     * JSON Schema object describing the function parameters that the LLM may
     * supply. Must NOT include a user_id field — the tool retrieves the
     * current user internally via SecurityUtil.
     *
     * <p>Return an empty-properties schema for zero-argument tools:
     * {@code {"type":"object","properties":{}}}</p>
     */
    Map<String, Object> getParameterSchema();

    /**
     * Execute the tool.
     *
     * @param args   parsed JSON arguments from the LLM's tool_call payload
     * @param userId the authenticated user — injected by the caller, not the LLM
     * @return a concise string result injected back into the model context
     */
    String execute(Map<String, Object> args, Long userId);
}
