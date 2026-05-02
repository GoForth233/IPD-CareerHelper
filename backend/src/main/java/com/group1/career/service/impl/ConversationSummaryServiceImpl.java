package com.group1.career.service.impl;

import com.group1.career.model.entity.AssistantMessage;
import com.group1.career.model.entity.ConversationSummary;
import com.group1.career.repository.AssistantMessageRepository;
import com.group1.career.repository.ConversationSummaryRepository;
import com.group1.career.service.AiService;
import com.group1.career.service.ConversationSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * F11: qwen-turbo rolling summary implementation.
 *
 * <p>Threshold: if a session accumulates ≥ ROLL_THRESHOLD message rows
 * a summary is produced from the oldest ROLL_WINDOW messages and upserted
 * into {@code conversation_summaries}. The window is then "committed":
 * the summarized messages are NOT deleted (they remain for audit) but the
 * summary row now represents everything up to this point.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationSummaryServiceImpl implements ConversationSummaryService {

    private final AssistantMessageRepository messageRepository;
    private final ConversationSummaryRepository summaryRepository;
    private final AiService aiService;

    /** Messages in a session before a roll-up is triggered. */
    private static final int ROLL_THRESHOLD = 20;

    /** How many messages to include in the summarization prompt. */
    private static final int ROLL_WINDOW = 20;

    @Override
    public String getLatestSummary(Long userId, String persona) {
        return summaryRepository.findByUserIdAndPersona(userId, persona)
                .map(ConversationSummary::getSummaryText)
                .orElse("");
    }

    @Async
    @Override
    @Transactional
    public void triggerRollupIfNeeded(Long userId, String persona, Long sessionId) {
        try {
            List<AssistantMessage> msgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            if (msgs.size() < ROLL_THRESHOLD) return;

            log.info("[F11] Triggering summary rollup for user={} persona={} session={} msgCount={}",
                    userId, persona, sessionId, msgs.size());

            List<AssistantMessage> window = msgs.subList(0, Math.min(ROLL_WINDOW, msgs.size()));

            StringBuilder transcript = new StringBuilder();
            for (AssistantMessage m : window) {
                transcript.append(m.getRole().name()).append(": ").append(m.getContent()).append("\n");
            }

            Optional<ConversationSummary> existing = summaryRepository.findByUserIdAndPersona(userId, persona);
            String prevSummary = existing.map(ConversationSummary::getSummaryText).orElse("");

            String prompt = buildSummaryPrompt(prevSummary, transcript.toString());
            List<Map<String, String>> promptMessages = new ArrayList<>();
            promptMessages.add(Map.of("role", "user", "content", prompt));

            String newSummary = aiService.chat(promptMessages);

            if (newSummary == null || newSummary.startsWith("Error") || newSummary.startsWith("AI service busy")) {
                log.warn("[F11] Summary generation failed for user={}, skipping upsert", userId);
                return;
            }

            ConversationSummary row = existing.orElseGet(() -> ConversationSummary.builder()
                    .userId(userId)
                    .persona(persona)
                    .build());

            row.setSummaryText(newSummary);
            row.setTurnCount(row.getTurnCount() == null ? window.size() : row.getTurnCount() + window.size());
            row.setModelUsed("qwen-max");
            summaryRepository.save(row);

            log.info("[F11] Summary saved for user={} persona={} turns={}", userId, persona, row.getTurnCount());
        } catch (Exception e) {
            log.error("[F11] Rollup failed for user={} persona={}: {}", userId, persona, e.getMessage(), e);
        }
    }

    private String buildSummaryPrompt(String prevSummary, String transcript) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a concise memory assistant. Summarize the following conversation into 3-5 sentences. ");
        sb.append("Focus on: the user's career goals, skills discussed, advice given, and any commitments made. ");
        sb.append("Reply ONLY with the summary paragraph, no preamble.\n\n");
        if (!prevSummary.isBlank()) {
            sb.append("PREVIOUS SUMMARY:\n").append(prevSummary).append("\n\n");
            sb.append("NEW CONVERSATION (extends the above):\n");
        } else {
            sb.append("CONVERSATION:\n");
        }
        sb.append(transcript);
        return sb.toString();
    }
}
