package com.group1.career.service;

import java.util.List;
import java.util.Map;

public interface AiService {
    /**
     * Chat with AI (Multi-turn)
     * @param messages List of messages [{"role": "user", "content": "..."}]
     * @return AI response content
     */
    String chat(List<Map<String, String>> messages);
}
