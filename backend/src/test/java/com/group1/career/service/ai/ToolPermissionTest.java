package com.group1.career.service.ai;

import com.group1.career.service.ai.tools.AiTool;
import com.group1.career.service.ai.tools.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F13: Security regression test — ensures that no tool schema exposes a
 * {@code user_id} parameter to the LLM.
 *
 * <p>Background: the LLM must never be able to hallucinate a different
 * {@code user_id} and pass it as a tool argument to access another user's
 * data. Tool implementations receive the authenticated user's ID via the
 * {@code execute(args, userId)} parameter, not from the LLM payload.
 * This test verifies that the schemas registered in {@link ToolRegistry}
 * do not accidentally declare a {@code user_id} property.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class ToolPermissionTest {

    @Autowired
    private ToolRegistry toolRegistry;

    @Test
    void allToolSchemas_mustNotExposeUserIdParameter() {
        List<AiTool> tools = toolRegistry.all();
        assertThat(tools).isNotEmpty();

        for (AiTool tool : tools) {
            Map<String, Object> schema = tool.getParameterSchema();
            assertThat(schema).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            if (properties != null) {
                assertThat(properties)
                        .as("Tool '%s' must NOT expose a user_id parameter in its schema", tool.getName())
                        .doesNotContainKey("user_id")
                        .doesNotContainKey("userId")
                        .doesNotContainKey("uid");
            }
        }
    }

    @Test
    void allToolSchemas_haveNonBlankNameAndDescription() {
        for (AiTool tool : toolRegistry.all()) {
            assertThat(tool.getName())
                    .as("Tool name must be non-blank")
                    .isNotBlank()
                    .matches("[a-z_]+");  // snake_case only
            assertThat(tool.getDescription())
                    .as("Tool '%s' must have a non-blank description", tool.getName())
                    .isNotBlank()
                    .hasSizeGreaterThan(10);
        }
    }

    @Test
    void toolRegistry_buildToolSchemas_producesOpenAiCompatibleFormat() {
        List<Map<String, Object>> schemas = toolRegistry.buildToolSchemas();
        assertThat(schemas).isNotEmpty();
        for (Map<String, Object> schema : schemas) {
            assertThat(schema).containsKey("type");
            assertThat(schema.get("type")).isEqualTo("function");
            assertThat(schema).containsKey("function");

            @SuppressWarnings("unchecked")
            Map<String, Object> fn = (Map<String, Object>) schema.get("function");
            assertThat(fn).containsKeys("name", "description", "parameters");
        }
    }
}
