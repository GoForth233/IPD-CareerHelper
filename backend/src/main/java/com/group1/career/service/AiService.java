package com.group1.career.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface AiService {
    /**
     * Chat with AI (Multi-turn, synchronous)
     */
    String chat(List<Map<String, String>> messages);

    /**
     * Chat with AI (Single-turn shortcut)
     */
    String chat(String prompt);

    /**
     * Stream chat with AI via SSE (Multi-turn, token-by-token)
     */
    SseEmitter streamChat(List<Map<String, String>> messages);
}
