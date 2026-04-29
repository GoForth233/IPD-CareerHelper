package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.service.AiService;
import com.group1.career.service.InterviewService;
import com.group1.career.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interview chat lifecycle. Every endpoint resolves the caller via JWT
 * (SecurityUtil) and enforces ownership through InterviewService.assertOwnership.
 * The userId is never trusted from the request body.
 */
@Slf4j
@Tag(name = "Interview API")
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final AiService aiService;

    @Operation(summary = "Start a new interview (or reuse an existing ONGOING session)")
    @PostMapping("/start")
    public Result<Interview> startInterview(@RequestBody StartInterviewRequest request) {
        Long uid = SecurityUtil.requireCurrentUserId();

        // Reuse any existing ONGOING session instead of spawning a duplicate
        List<Interview> activeOnes = interviewService.getUserInterviews(uid)
                .stream()
                .filter(i -> "ONGOING".equals(i.getStatus()))
                .toList();
        if (!activeOnes.isEmpty()) {
            return Result.success(activeOnes.get(0));
        }

        Interview interview = interviewService.startInterview(
                uid,
                request.getResumeId(),
                request.getPositionName(),
                request.getDifficulty()
        );
        return Result.success(interview);
    }

    @Operation(summary = "Generate the AI interviewer's opening question. Idempotent: " +
            "returns the existing first message if the conversation is non-empty.")
    @PostMapping("/{interviewId}/greeting")
    public Result<InterviewMessage> generateGreeting(@PathVariable Long interviewId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        Interview interview = interviewService.assertOwnership(interviewId, uid);

        List<InterviewMessage> existing = interviewService.getInterviewMessages(interviewId);
        if (!existing.isEmpty()) {
            return Result.success(existing.get(0));
        }

        String prompt = String.format(
                "You are a senior technical interviewer for the role of \"%s\" (difficulty: %s). " +
                "Greet the candidate in ONE short paragraph (2-3 sentences) and ask the very first " +
                "interview question. Do NOT include explanations, evaluation criteria, or follow-ups. " +
                "Reply in plain text, no markdown.",
                interview.getPositionName(),
                interview.getDifficulty() == null ? "Normal" : interview.getDifficulty()
        );
        String greeting = aiService.chat(prompt);
        if (greeting == null || greeting.isBlank()) {
            throw new BizException("AI failed to generate the opening question");
        }
        interviewService.sendMessage(interviewId, "AI", greeting.trim());
        return Result.success(interviewService.getInterviewMessages(interviewId).get(0));
    }

    @Operation(summary = "Send a message in interview (with AI response)")
    @PostMapping("/{interviewId}/message")
    public Result<MessageResponse> sendMessage(
            @PathVariable Long interviewId,
            @RequestBody SendMessageRequest request
    ) {
        Long uid = SecurityUtil.requireCurrentUserId();
        Interview interview = interviewService.assertOwnership(interviewId, uid);

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BizException("Message content is required");
        }

        // 1. Save user message
        interviewService.sendMessage(interviewId, "USER", request.getContent());

        // 2. Build chat completion payload (system + full transcript so far)
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", String.format(
                "You are a professional %s interviewer (difficulty: %s). Continue the interview by " +
                "briefly evaluating the candidate's last answer and then asking the next focused " +
                "question. Keep replies short (2-4 sentences). Plain text only, no markdown.",
                interview.getPositionName(),
                interview.getDifficulty() == null ? "Normal" : interview.getDifficulty()
        ));
        messages.add(systemMsg);

        for (InterviewMessage msg : interviewService.getInterviewMessages(interviewId)) {
            Map<String, String> historyMsg = new HashMap<>();
            historyMsg.put("role", "USER".equalsIgnoreCase(msg.getRole()) ? "user" : "assistant");
            historyMsg.put("content", msg.getContent());
            messages.add(historyMsg);
        }

        String aiResponse = aiService.chat(messages);
        interviewService.sendMessage(interviewId, "AI", aiResponse);

        MessageResponse response = new MessageResponse();
        response.setUserMessage(request.getContent());
        response.setAiMessage(aiResponse);
        return Result.success(response);
    }

    @Operation(summary = "Get interview message history")
    @GetMapping("/{interviewId}/messages")
    public Result<List<InterviewMessage>> getMessages(@PathVariable Long interviewId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        interviewService.assertOwnership(interviewId, uid);
        return Result.success(interviewService.getInterviewMessages(interviewId));
    }

    @Operation(summary = "End interview. Final score is computed lazily by the report endpoint.")
    @PostMapping("/{interviewId}/end")
    public Result<Interview> endInterview(@PathVariable Long interviewId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        interviewService.assertOwnership(interviewId, uid);
        // No score is taken from the client. Pass null so endInterview() only flips state.
        return Result.success(interviewService.endInterview(interviewId, null));
    }

    @Operation(summary = "Get the current user's interviews (preferred)")
    @GetMapping("/user/me")
    public Result<List<Interview>> getMyInterviews() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(interviewService.getUserInterviews(uid));
    }

    @Operation(summary = "Get user's all interviews (legacy: path userId must equal JWT subject)")
    @GetMapping("/user/{userId}")
    public Result<List<Interview>> getUserInterviews(@PathVariable Long userId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        if (!uid.equals(userId)) {
            throw new BizException("Cannot list another user's interviews");
        }
        return Result.success(interviewService.getUserInterviews(userId));
    }

    @Operation(summary = "Get interview by ID")
    @GetMapping("/{interviewId}")
    public Result<Interview> getInterview(@PathVariable Long interviewId) {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(interviewService.assertOwnership(interviewId, uid));
    }

    // ================= DTO Classes =================

    @Data
    public static class StartInterviewRequest {
        // userId is intentionally absent; resolved from JWT
        private Long resumeId;
        private String positionName;
        private String difficulty; // Easy, Normal, Hard
    }

    @Data
    public static class SendMessageRequest {
        private String content;
    }

    @Data
    public static class MessageResponse {
        private String userMessage;
        private String aiMessage;
    }
}
