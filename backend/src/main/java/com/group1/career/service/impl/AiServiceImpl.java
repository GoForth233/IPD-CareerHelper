package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.career.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${aliyun.ai.api-key}")
    private String apiKey;

    @Value("${aliyun.ai.model:qwen-max}")
    private String modelName;

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20)) // Longer timeout for chat
            .build();

    @Override
    public String chat(List<Map<String, String>> messages) {
        try {
            // 1. Build Request Body
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", modelName);
            
            ArrayNode msgArray = root.putArray("messages");
            
            // Add System Prompt if not present (optional, can be done by frontend)
            // But here we ensure consistent persona
            ObjectNode systemMsg = msgArray.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a professional resume analysis assistant. Help users improve their resumes.");

            // Append History
            for (Map<String, String> msg : messages) {
                ObjectNode node = msgArray.addObject();
                node.put("role", msg.get("role"));
                node.put("content", msg.get("content"));
            }

            String requestBody = objectMapper.writeValueAsString(root);

            // 2. Send Request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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
    
    // Deprecated single-turn diagnosis (kept for compatibility if needed, or removed)
    public String diagnose(String content) {
        return chat(List.of(Map.of("role", "user", "content", content)));
    }
}
