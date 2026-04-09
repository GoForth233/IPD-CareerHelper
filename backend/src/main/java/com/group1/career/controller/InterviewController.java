package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.service.AiService;
import com.group1.career.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Interview API")
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final AiService aiService;

    @Operation(summary = "Start a new interview (with session guard)")
    @PostMapping("/start")
    public Result<Interview> startInterview(@RequestBody StartInterviewRequest request) {
        // Guard: check if user already has an active session
        List<Interview> activeOnes = interviewService.getUserInterviews(request.getUserId())
                .stream()
                .filter(i -> "ONGOING".equals(i.getStatus()))
                .toList();
        if (!activeOnes.isEmpty()) {
            // Return the existing session instead of creating a duplicate
            return Result.success(activeOnes.get(0));
        }

        Interview interview = interviewService.startInterview(
                request.getUserId(),
                request.getResumeId(),
                request.getPositionName(),
                request.getDifficulty()
        );
        return Result.success(interview);
    }

    @Operation(summary = "Get active session status for a user")
    @GetMapping("/session/status")
    public Result<SessionStatusDto> getSessionStatus(@RequestParam Long userId) {
        List<Interview> interviews = interviewService.getUserInterviews(userId);
        Interview active = interviews.stream()
                .filter(i -> "ONGOING".equals(i.getStatus()))
                .findFirst()
                .orElse(null);

        SessionStatusDto status = new SessionStatusDto();
        status.setHasActiveSession(active != null);
        status.setActiveInterview(active);
        status.setTotalCompleted((int) interviews.stream().filter(i -> "COMPLETED".equals(i.getStatus())).count());
        return Result.success(status);
    }

    @Operation(summary = "Send a message in interview (with AI response)")
    @PostMapping("/{interviewId}/message")
    public Result<MessageResponse> sendMessage(
            @PathVariable Long interviewId,
            @RequestBody SendMessageRequest request
    ) {
        // 1. Save user message
        interviewService.sendMessage(interviewId, "USER", request.getContent());

        // 2. Call AI to get response
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Add system prompt for interview
        Interview interview = interviewService.getInterviewById(interviewId);
        String systemPrompt = String.format(
                "You are a professional interviewer for the position of %s. " +
                "Conduct a technical interview by asking relevant questions and evaluating the candidate's answers. " +
                "Be professional, encouraging, and provide constructive feedback.",
                interview.getPositionName()
        );
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        // Add conversation history
        List<InterviewMessage> history = interviewService.getInterviewMessages(interviewId);
        for (InterviewMessage msg : history) {
            Map<String, String> historyMsg = new HashMap<>();
            historyMsg.put("role", msg.getRole().equalsIgnoreCase("USER") ? "user" : "assistant");
            historyMsg.put("content", msg.getContent());
            messages.add(historyMsg);
        }

        String aiResponse = aiService.chat(messages);

        // 3. Save AI response
        interviewService.sendMessage(interviewId, "AI", aiResponse);

        MessageResponse response = new MessageResponse();
        response.setUserMessage(request.getContent());
        response.setAiMessage(aiResponse);

        return Result.success(response);
    }

    @Operation(summary = "Get interview message history")
    @GetMapping("/{interviewId}/messages")
    public Result<List<InterviewMessage>> getMessages(@PathVariable Long interviewId) {
        List<InterviewMessage> messages = interviewService.getInterviewMessages(interviewId);
        return Result.success(messages);
    }

    @Operation(summary = "End interview")
    @PostMapping("/{interviewId}/end")
    public Result<Interview> endInterview(
            @PathVariable Long interviewId,
            @RequestBody EndInterviewRequest request
    ) {
        Interview interview = interviewService.endInterview(interviewId, request.getFinalScore());
        return Result.success(interview);
    }

    @Operation(summary = "Get user's all interviews")
    @GetMapping("/user/{userId}")
    public Result<List<Interview>> getUserInterviews(@PathVariable Long userId) {
        List<Interview> interviews = interviewService.getUserInterviews(userId);
        return Result.success(interviews);
    }

    @Operation(summary = "Get interview by ID")
    @GetMapping("/{interviewId}")
    public Result<Interview> getInterview(@PathVariable Long interviewId) {
        Interview interview = interviewService.getInterviewById(interviewId);
        return Result.success(interview);
    }

    // ================= DTO Classes =================

    @Data
    public static class StartInterviewRequest {
        private Long userId;
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

    @Data
    public static class EndInterviewRequest {
        private Integer finalScore;
    }

    @Data
    public static class SessionStatusDto {
        private boolean hasActiveSession;
        private Interview activeInterview;
        private int totalCompleted;
    }
}

