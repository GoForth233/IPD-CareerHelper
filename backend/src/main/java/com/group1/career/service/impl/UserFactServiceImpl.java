package com.group1.career.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.AssistantMessage;
import com.group1.career.model.entity.UserFact;
import com.group1.career.repository.AssistantMessageRepository;
import com.group1.career.repository.UserFactRepository;
import com.group1.career.service.AiService;
import com.group1.career.service.UserFactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * F12: qwen-turbo-based user fact extraction.
 *
 * <p>Strategy: read the last {@code EXTRACT_WINDOW} user messages from a
 * session, ask qwen-turbo to return a JSON array of facts, then upsert each
 * by {@code fact_key}. High-confidence facts (≥ 0.90) may overwrite lower
 * confidence ones for the same key.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFactServiceImpl implements UserFactService {

    private final AssistantMessageRepository messageRepository;
    private final UserFactRepository factRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Only inspect the most recent N user messages per extraction call. */
    private static final int EXTRACT_WINDOW = 10;

    @Override
    public List<UserFact> getUserFacts(Long userId) {
        return factRepository.findByUserId(userId);
    }

    @Async
    @Override
    @Transactional
    public void extractAndSaveAsync(Long userId, Long sessionId) {
        try {
            List<AssistantMessage> allMsgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            List<AssistantMessage> userMsgs = allMsgs.stream()
                    .filter(m -> m.getRole() == AssistantMessage.MessageRole.user)
                    .toList();

            if (userMsgs.size() < 3) return; // not enough data yet

            int start = Math.max(0, userMsgs.size() - EXTRACT_WINDOW);
            List<AssistantMessage> window = userMsgs.subList(start, userMsgs.size());

            StringBuilder transcript = new StringBuilder();
            for (AssistantMessage m : window) {
                transcript.append("user: ").append(m.getContent()).append("\n");
            }

            String prompt = buildExtractionPrompt(transcript.toString());
            String raw = aiService.chat(
                    List.of(Map.of("role", "user", "content", prompt))
            );

            if (raw == null || raw.startsWith("Error") || raw.startsWith("AI service busy")) {
                log.warn("[F12] Fact extraction failed for user={}, response={}", userId, raw);
                return;
            }

            List<FactDto> facts = parseFacts(raw);
            upsertFacts(userId, facts);
            log.info("[F12] Extracted {} facts for user={}", facts.size(), userId);

        } catch (Exception e) {
            log.error("[F12] Extraction error for user={} session={}: {}", userId, sessionId, e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFact(Long userId, Long factId) {
        Optional<UserFact> opt = factRepository.findById(factId);
        if (opt.isEmpty()) return false;
        UserFact fact = opt.get();
        if (!userId.equals(fact.getUserId())) return false;
        factRepository.delete(fact);
        return true;
    }

    @Override
    public String renderForPrompt(Long userId) {
        List<UserFact> facts = factRepository.findByUserId(userId);
        if (facts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("[USER KNOWN FACTS]\n");
        for (UserFact f : facts) {
            sb.append("- ").append(f.getFactKey()).append(": ").append(f.getFactValue()).append("\n");
        }
        return sb.toString();
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private void upsertFacts(Long userId, List<FactDto> facts) {
        for (FactDto dto : facts) {
            if (dto.factKey == null || dto.factKey.isBlank() || dto.factValue == null) continue;
            Optional<UserFact> existing = factRepository.findByUserIdAndFactKey(userId, dto.factKey);
            BigDecimal newConf = dto.confidence != null ? dto.confidence : BigDecimal.ONE;

            if (existing.isPresent()) {
                UserFact f = existing.get();
                // Only overwrite if new confidence is higher or equal
                if (newConf.compareTo(f.getConfidence()) >= 0) {
                    f.setFactValue(dto.factValue);
                    f.setConfidence(newConf);
                    if (dto.category != null) f.setCategory(dto.category);
                    factRepository.save(f);
                }
            } else {
                factRepository.save(UserFact.builder()
                        .userId(userId)
                        .category(dto.category != null ? dto.category : "GENERAL")
                        .factKey(dto.factKey)
                        .factValue(dto.factValue)
                        .confidence(newConf)
                        .source("AI_EXTRACTED")
                        .build());
            }
        }
    }

    private List<FactDto> parseFacts(String raw) {
        try {
            // Model may wrap JSON in markdown code fences — strip them
            String json = raw.trim();
            if (json.startsWith("```")) {
                int start = json.indexOf('\n') + 1;
                int end = json.lastIndexOf("```");
                if (end > start) json = json.substring(start, end).trim();
            }
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("[F12] Could not parse facts JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String buildExtractionPrompt(String userMessages) {
        return """
                You are a fact extractor. Read the following user messages and extract structured facts about the user.
                Output ONLY a valid JSON array (no explanation, no markdown fences). Each element must have:
                  - "category": one of PERSONALITY, CAREER_GOAL, SKILL, PREFERENCE, EXPERIENCE
                  - "factKey": a short snake_case key, e.g. "mbti_type", "target_role", "weak_skill_sql"
                  - "factValue": the fact as a concise string
                  - "confidence": a float 0.0–1.0 (1.0 = user explicitly stated it)
                Only include facts that are clearly stated or strongly implied. Return [] if nothing useful.

                USER MESSAGES:
                """ + userMessages;
    }

    /** DTO for deserialization of the LLM response. */
    private static class FactDto {
        public String category;
        public String factKey;
        public String factValue;
        public BigDecimal confidence;
    }
}
