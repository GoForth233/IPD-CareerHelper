package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.document.ChatSessionDocument;
import com.group1.career.repository.ChatSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Tag(name = "Chat History API", description = "Cloud-synced chat session persistence for multi-device roaming")
@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatSessionRepository chatSessionRepository;

    @Operation(summary = "Get all chat sessions for a user (sorted by latest)")
    @GetMapping("/{userId}")
    public Result<List<ChatSessionDocument>> getUserSessions(@PathVariable Long userId) {
        List<ChatSessionDocument> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return Result.success(sessions);
    }

    @Operation(summary = "Get a specific session by ID")
    @GetMapping("/session/{sessionId}")
    public Result<ChatSessionDocument> getSession(@PathVariable String sessionId) {
        ChatSessionDocument session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return Result.success(session);
    }

    @Operation(summary = "Create a new chat session")
    @PostMapping("/create")
    public Result<ChatSessionDocument> createSession(@RequestBody CreateSessionDto request) {
        ChatSessionDocument session = ChatSessionDocument.builder()
                .userId(request.getUserId())
                .title(request.getTitle() != null ? request.getTitle() : "New Conversation")
                .messages(new ArrayList<>())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        return Result.success(chatSessionRepository.save(session));
    }

    @Operation(summary = "Append a message pair to an existing session")
    @PostMapping("/session/{sessionId}/append")
    public Result<ChatSessionDocument> appendMessage(
            @PathVariable String sessionId,
            @RequestBody AppendMessageDto request) {

        ChatSessionDocument session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Append user message
        session.getMessages().add(ChatSessionDocument.ChatMessageItem.builder()
                .role("user")
                .content(request.getUserMessage())
                .timestamp(new Date())
                .build());

        // Append assistant reply
        session.getMessages().add(ChatSessionDocument.ChatMessageItem.builder()
                .role("assistant")
                .content(request.getAssistantReply())
                .timestamp(new Date())
                .build());

        // Auto-generate title from first message if still default
        if ("New Conversation".equals(session.getTitle()) && !session.getMessages().isEmpty()) {
            String firstMsg = session.getMessages().get(0).getContent();
            session.setTitle(firstMsg.length() > 20 ? firstMsg.substring(0, 20) + "..." : firstMsg);
        }

        session.setUpdatedAt(new Date());
        return Result.success(chatSessionRepository.save(session));
    }

    @Operation(summary = "Delete a chat session")
    @DeleteMapping("/session/{sessionId}")
    public Result<String> deleteSession(@PathVariable String sessionId) {
        chatSessionRepository.deleteById(sessionId);
        return Result.success("Session deleted");
    }

    // ================= DTO Classes =================

    @Data
    public static class CreateSessionDto {
        private Long userId;
        private String title;
    }

    @Data
    public static class AppendMessageDto {
        private String userMessage;
        private String assistantReply;
    }
}
