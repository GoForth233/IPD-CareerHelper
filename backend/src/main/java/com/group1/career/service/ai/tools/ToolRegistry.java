package com.group1.career.service.ai.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * F13: Central registry for all AI tool implementations.
 *
 * <p>Spring auto-wires every {@link AiTool} bean into this registry.
 * {@link com.group1.career.service.ai.FunctionCallingService} uses this
 * to look up a tool by name when the LLM issues a tool_call.</p>
 */
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<AiTool> tools;

    private Map<String, AiTool> byName;

    private Map<String, AiTool> getIndex() {
        if (byName == null) {
            byName = tools.stream().collect(Collectors.toMap(AiTool::getName, Function.identity()));
        }
        return byName;
    }

    /** All registered tools as OpenAI-compatible tool schema objects. */
    public List<Map<String, Object>> buildToolSchemas() {
        return tools.stream().map(t -> Map.of(
                "type", "function",
                "function", Map.of(
                        "name", t.getName(),
                        "description", t.getDescription(),
                        "parameters", t.getParameterSchema()
                )
        )).collect(Collectors.toList());
    }

    /** Look up a tool by its name. Empty if not found. */
    public Optional<AiTool> find(String name) {
        return Optional.ofNullable(getIndex().get(name));
    }

    public List<AiTool> all() {
        return tools;
    }
}
