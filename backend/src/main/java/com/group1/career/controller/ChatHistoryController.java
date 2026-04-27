package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.AssistantMessage;
import com.group1.career.model.entity.AssistantSession;
import com.group1.career.repository.AssistantMessageRepository;
import com.group1.career.repository.AssistantSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat History API", description = "Cloud-synced chat session persistence")
@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final AssistantSessionRepository sessionRepository;
    private final AssistantMessageRepository messageRepository;

    @Operation(summary = "Get all chat sessions for a user (sorted by latest)")
    @GetMapping("/{userId}")
    public Result<List<AssistantSession>> getUserSessions(@PathVariable Long userId) {
        return Result.success(sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @Operation(summary = "Get all messages in a session")
    @GetMapping("/session/{sessionId}")
    public Result<List<AssistantMessage>> getSessionMessages(@PathVariable Long sessionId) {
        return Result.success(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    @Operation(summary = "Create a new chat session")
    @PostMapping("/create")
    public Result<AssistantSession> createSession(@RequestBody CreateSessionDto request) {
        AssistantSession session = AssistantSession.builder()
                .userId(request.getUserId())
                .title(request.getTitle() != null ? request.getTitle() : "New Conversation")
                .build();
        return Result.success(sessionRepository.save(session));
    }

    @Operation(summary = "Append a message pair to an existing session")
    @PostMapping("/session/{sessionId}/append")
    @Transactional
    public Result<AssistantSession> appendMessage(
            @PathVariable Long sessionId,
            @RequestBody AppendMessageDto request) {

        AssistantSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        messageRepository.save(AssistantMessage.builder()
                .sessionId(sessionId)
                .role(AssistantMessage.MessageRole.user)
                .content(request.getUserMessage())
                .build());

        messageRepository.save(AssistantMessage.builder()
                .sessionId(sessionId)
                .role(AssistantMessage.MessageRole.assistant)
                .content(request.getAssistantReply())
                .build());

        if ("New Conversation".equals(session.getTitle())) {
            String firstMsg = request.getUserMessage();
            session.setTitle(firstMsg.length() > 20 ? firstMsg.substring(0, 20) + "..." : firstMsg);
            sessionRepository.save(session);
        }

        return Result.success(session);
    }

    @Operation(summary = "Delete a chat session and all its messages")
    @DeleteMapping("/session/{sessionId}")
    @Transactional
    public Result<String> deleteSession(@PathVariable Long sessionId) {
        messageRepository.deleteBySessionId(sessionId);
        sessionRepository.deleteById(sessionId);
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
