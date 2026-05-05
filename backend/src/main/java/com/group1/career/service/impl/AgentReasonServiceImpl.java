package com.group1.career.service.impl;

import com.group1.career.service.AgentReasonService;
import com.group1.career.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentReasonServiceImpl implements AgentReasonService {

    private final AiService aiService;
    private final StringRedisTemplate redis;

    private static final String MODEL = "qwen-turbo";
    private static final long CACHE_TTL_SECONDS = 4 * 60 * 60;
    private static final long LLM_TIMEOUT_MS = 8_000;
    private static final String KEY_PREFIX = "agent:reason:";

    @Override
    public String reason(Long userId, String stage, String context, String fallback) {
        String cacheKey = KEY_PREFIX + userId + ":" + stage;

        // 1. Serve from cache if present
        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                log.debug("[AgentReason] cache hit user={} stage={}", userId, stage);
                return cached;
            }
        } catch (Exception e) {
            log.warn("[AgentReason] Redis read failed: {}", e.getMessage());
        }

        // 2. Call qwen-turbo asynchronously with a hard timeout
        try {
            String prompt = buildPrompt(stage, context);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(
                    () -> aiService.chat(
                            List.of(
                                    Map.of("role", "system", "content",
                                            "你是一个职业发展 Agent。用一句简洁的中文给用户解释今天的核心行动理由，不超过 40 字，语气积极。"),
                                    Map.of("role", "user", "content", prompt)
                            ),
                            MODEL
                    )
            );

            String result = future.get(LLM_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // Validate: must be a plausible Chinese sentence, not a fallback marker
            if (result != null && !result.isBlank() && !result.contains("AI 助手暂时繁忙")) {
                String trimmed = result.trim();
                // 3. Cache the result
                try {
                    redis.opsForValue().set(cacheKey, trimmed, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                } catch (Exception re) {
                    log.warn("[AgentReason] Redis write failed: {}", re.getMessage());
                }
                log.info("[AgentReason] LLM generated reason user={} stage={}", userId, stage);
                return trimmed;
            }
        } catch (java.util.concurrent.TimeoutException te) {
            log.warn("[AgentReason] LLM timeout after {}ms user={} stage={}", LLM_TIMEOUT_MS, userId, stage);
        } catch (Exception e) {
            log.warn("[AgentReason] LLM call failed user={} stage={}: {}", userId, stage, e.getMessage());
        }

        // 4. Fallback to rule-based reason
        return fallback;
    }

    private String buildPrompt(String stage, String context) {
        return "当前求职阶段：" + stage + "。上下文：" + context + "。请用一句话说明用户今天最应该做什么，以及原因。";
    }
}
