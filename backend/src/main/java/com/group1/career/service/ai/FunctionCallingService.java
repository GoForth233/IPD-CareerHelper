package com.group1.career.service.ai;

import java.util.List;
import java.util.Map;

/**
 * F13: Orchestrates the OpenAI-compatible function-calling loop.
 *
 * <p>Sends messages + tool schemas to the model, intercepts tool_call
 * responses, executes tools with the authenticated user's ID, appends
 * results, and repeats until the model returns a final text response
 * or the iteration cap is hit.</p>
 */
public interface FunctionCallingService {

    /**
     * Run a full agentic turn: may call tools 0-{@code MAX_TOOL_CALLS} times
     * before producing the final text reply.
     *
     * @param messages conversation history (role/content maps)
     * @param userId   authenticated user — injected into every tool call
     * @return final assistant text response
     */
    String chat(List<Map<String, String>> messages, Long userId);
}
