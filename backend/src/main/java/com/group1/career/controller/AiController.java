package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "AI Service")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "Chat with AI (Multi-turn)")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody ChatRequest request) {
        return Result.success(aiService.chat(request.getMessages()));
    }

    @Data
    public static class ChatRequest {
        private List<Map<String, String>> messages; // [{"role": "user", "content": "..."}]
    }
}
