package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Chat Assistant API", description = "Global AI career assistant with SSE streaming")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiService aiService;

    private static final String SYSTEM_PROMPT =
            "You are '小职' (Little Career), a friendly and professional AI career planning assistant. " +
            "Help college students with career planning, resume writing, interview preparation, " +
            "and skill development advice. Always respond in a warm, encouraging tone. " +
            "If the user asks something unrelated to careers, gently guide them back to career topics.";

    @Operation(summary = "Send chat message (synchronous response)")
    @PostMapping("/send")
    public Result<ChatResponseDto> sendMessage(@RequestBody ChatRequestDto request) {
        List<Map<String, String>> messages = buildMessages(request.getHistory(), request.getMessage());
        String reply = aiService.chat(messages);

        ChatResponseDto response = new ChatResponseDto();
        response.setReply(reply);
        return Result.success(response);
    }

    @Operation(summary = "Send chat message (SSE streaming response)")
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter streamMessage(
            @RequestParam String message,
            @RequestParam(required = false) String historyJson) {

        List<Map<String, String>> history = new ArrayList<>();
        // In production, parse historyJson from the frontend
        List<Map<String, String>> messages = buildMessages(history, message);
        return aiService.streamChat(messages);
    }

    private List<Map<String, String>> buildMessages(List<Map<String, String>> history, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // Conversation history (context memory window)
        if (history != null) {
            messages.addAll(history);
        }

        // Current user message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        return messages;
    }

    // ================= DTO Classes =================

    @Data
    public static class ChatRequestDto {
        private String message;
        private List<Map<String, String>> history;
    }

    @Data
    public static class ChatResponseDto {
        private String reply;
    }
}
