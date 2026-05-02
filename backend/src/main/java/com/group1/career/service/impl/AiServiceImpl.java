package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.career.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${aliyun.ai.api-key}")
    private String apiKey;

    @Value("${aliyun.ai.model:qwen-max}")
    private String modelName;

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60)) // Longer timeout for streaming
            .build();

    /**
     * Default persona used only when the caller did NOT supply their own
     * {@code system} message. Callers like InterviewController and ChatController
     * inject task-specific personas; double-stacking systems used to confuse
     * Qwen and produce off-topic replies.
     */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are a professional career assistant. Reply concisely and stay on topic.";

    @Override
    public String chat(List<Map<String, String>> messages) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", modelName);

            ArrayNode msgArray = root.putArray("messages");

            // Inject the default persona only when the caller hasn't already
            // supplied their own system prompt. Stacking two system messages
            // can knock Qwen off the role the caller actually wanted.
            boolean callerHasSystem = messages != null
                    && !messages.isEmpty()
                    && "system".equalsIgnoreCase(messages.get(0).get("role"));
            if (!callerHasSystem) {
                ObjectNode systemMsg = msgArray.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", DEFAULT_SYSTEM_PROMPT);
            }

            if (messages != null) {
                for (Map<String, String> msg : messages) {
                    ObjectNode node = msgArray.addObject();
                    node.put("role", msg.get("role"));
                    node.put("content", msg.get("content"));
                }
            }

            String requestBody = objectMapper.writeValueAsString(root);

            // 2. Send Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(110)) // hard cap so the request never hangs forever
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            long aiStart = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("[AI] {} ms, status={}, prompt={} chars", System.currentTimeMillis() - aiStart, response.statusCode(), requestBody.length());

            if (response.statusCode() == 200) {
                // 3. Parse Response
                ObjectNode resJson = (ObjectNode) objectMapper.readTree(response.body());
                if (resJson.has("choices") && resJson.get("choices").size() > 0) {
                    return resJson.get("choices").get(0).get("message").get("content").asText();
                }
            }
            
            log.error("AI API Error: {}", response.body());
            return "AI service busy (Error: " + response.statusCode() + ")";

        } catch (Exception e) {
            log.error("AI Chat Failed", e);
            return "Error: " + e.getMessage();
        }
    }
    
    @Override
    public String chat(List<Map<String, String>> messages, String model) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model != null ? model : modelName);

            ArrayNode msgArray = root.putArray("messages");
            boolean callerHasSystem = messages != null
                    && !messages.isEmpty()
                    && "system".equalsIgnoreCase(messages.get(0).get("role"));
            if (!callerHasSystem) {
                ObjectNode systemMsg = msgArray.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", DEFAULT_SYSTEM_PROMPT);
            }
            if (messages != null) {
                for (Map<String, String> msg : messages) {
                    ObjectNode node = msgArray.addObject();
                    node.put("role", msg.get("role"));
                    node.put("content", msg.get("content"));
                }
            }

            String requestBody = objectMapper.writeValueAsString(root);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                ObjectNode resJson = (ObjectNode) objectMapper.readTree(response.body());
                if (resJson.has("choices") && resJson.get("choices").size() > 0) {
                    return resJson.get("choices").get(0).get("message").get("content").asText();
                }
            }
            log.error("AI API Error (model={}): {}", model, response.body());
            return "AI service busy (Error: " + response.statusCode() + ")";
        } catch (Exception e) {
            log.error("AI Chat Failed (model={})", model, e);
            return "Error: " + e.getMessage();
        }
    }

    // Single-turn shortcut
    @Override
    public String chat(String prompt) {
        return chat(List.of(Map.of("role", "user", "content", prompt)));
    }

    @Override
    public String chatWithTools(List<Map<String, Object>> messages, List<Map<String, Object>> toolSchemas) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", modelName);
            root.put("tool_choice", "auto");

            // Build messages array — supports role=tool and assistant tool_call messages
            ArrayNode msgArray = root.putArray("messages");
            if (messages != null) {
                for (Map<String, Object> msg : messages) {
                    ObjectNode node = msgArray.addObject();
                    for (Map.Entry<String, Object> e : msg.entrySet()) {
                        if (e.getValue() == null) {
                            node.putNull(e.getKey());
                        } else if (e.getValue() instanceof String s) {
                            node.put(e.getKey(), s);
                        } else {
                            node.set(e.getKey(), objectMapper.valueToTree(e.getValue()));
                        }
                    }
                }
            }

            // Attach tool schemas
            if (toolSchemas != null && !toolSchemas.isEmpty()) {
                root.set("tools", objectMapper.valueToTree(toolSchemas));
            }

            String requestBody = objectMapper.writeValueAsString(root);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("[F13] chatWithTools error status={}: {}", response.statusCode(), response.body());
            }
            return response.body();
        } catch (Exception e) {
            log.error("[F13] chatWithTools failed: {}", e.getMessage(), e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    // Deprecated single-turn diagnosis (kept for compatibility)
    public String diagnose(String content) {
        return chat(List.of(Map.of("role", "user", "content", content)));
    }

    @Override
    public SseEmitter streamChat(List<Map<String, String>> messages) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        executorService.submit(() -> {
            try {
                // 1. Build request with stream:true
                ObjectNode root = objectMapper.createObjectNode();
                root.put("model", modelName);
                root.put("stream", true);

                ArrayNode msgArray = root.putArray("messages");
                for (Map<String, String> msg : messages) {
                    ObjectNode node = msgArray.addObject();
                    node.put("role", msg.get("role"));
                    node.put("content", msg.get("content"));
                }

                String requestBody = objectMapper.writeValueAsString(root);

                // 2. Send streaming request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<java.util.stream.Stream<String>> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofLines());

                // 3. Parse SSE lines and forward token-by-token
                response.body().forEach(line -> {
                    try {
                        if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                            String json = line.substring(6);
                            ObjectNode chunk = (ObjectNode) objectMapper.readTree(json);
                            if (chunk.has("choices") && chunk.get("choices").size() > 0) {
                                var delta = chunk.get("choices").get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String token = delta.get("content").asText();
                                    emitter.send(SseEmitter.event()
                                            .name("token")
                                            .data(token));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("SSE parse error: {}", e.getMessage());
                    }
                });

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (Exception e) {
                log.error("Stream chat failed", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
