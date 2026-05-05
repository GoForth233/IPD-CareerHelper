package com.group1.career.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.AgentTask;
import com.group1.career.repository.AgentTaskRepository;
import com.group1.career.service.AiService;
import com.group1.career.service.TaskDecomposer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Phase E5: decomposes a coarse-grained agent task into 2–4 concrete sub-tasks.
 *
 * <p>Strategy (in order of preference):
 * <ol>
 *   <li>Return existing sub-tasks if already decomposed (idempotent).</li>
 *   <li>Call qwen-turbo with a structured prompt; parse the JSON array response.</li>
 *   <li>Fall back to hard-coded rules keyed on {@code taskType} if AI fails.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDecomposerImpl implements TaskDecomposer {

    private final AgentTaskRepository taskRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public List<AgentTask> decompose(Long userId, Long parentTaskId) {
        AgentTask parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new BizException("Agent task not found"));
        if (!userId.equals(parent.getUserId())) {
            throw new BizException("Agent task not found");
        }

        if (taskRepository.existsByParentTaskId(parentTaskId)) {
            return taskRepository.findByParentTaskIdOrderBySubIndexAsc(parentTaskId);
        }

        List<SubTaskSpec> specs = decomposeThroughAi(parent);
        if (specs == null || specs.isEmpty()) {
            specs = decomposeThroughRules(parent);
        }

        List<AgentTask> saved = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            SubTaskSpec spec = specs.get(i);
            String subKey = parent.getTaskKey() + ":sub:" + i;
            AgentTask sub = AgentTask.builder()
                    .userId(userId)
                    .taskKey(subKey)
                    .title(spec.title())
                    .description(spec.description())
                    .taskType(parent.getTaskType())
                    .priority(parent.getPriority())
                    .status("TODO")
                    .target(parent.getTarget())
                    .source("DECOMPOSED")
                    .difficulty(spec.difficulty())
                    .estimatedMinutes(spec.estimatedMinutes())
                    .parentTaskId(parentTaskId)
                    .subIndex(i)
                    .dueDate(parent.getDueDate())
                    .build();
            saved.add(taskRepository.save(sub));
        }

        log.info("[task-decomposer] parent={} decomposed into {} sub-tasks", parentTaskId, saved.size());
        return saved;
    }

    // ── AI decomposition ──────────────────────────────────────────────────────

    private List<SubTaskSpec> decomposeThroughAi(AgentTask parent) {
        String prompt = buildPrompt(parent);
        try {
            String raw = aiService.chat(
                    List.of(Map.of("role", "user", "content", prompt)), "qwen-turbo");
            return parseAiResponse(raw);
        } catch (Exception e) {
            log.warn("[task-decomposer] AI decomposition failed for task={}: {}", parent.getTaskId(), e.getMessage());
            return null;
        }
    }

    private String buildPrompt(AgentTask parent) {
        return """
                You are a career planning assistant. Break the following career task into 2-4 concrete, actionable sub-tasks.
                Return ONLY a valid JSON array, no extra text. Each element must have exactly these fields:
                  "title" (string, ≤60 chars),
                  "description" (string, ≤120 chars),
                  "difficulty" (one of: EASY, MEDIUM, HARD),
                  "estimatedMinutes" (integer: 10, 15, 20, 30, or 60).

                Task title: %s
                Task type: %s
                """.formatted(parent.getTitle(), parent.getTaskType());
    }

    private List<SubTaskSpec> parseAiResponse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            String json = extractJsonArray(raw);
            List<Map<String, Object>> items = objectMapper.readValue(
                    json, new TypeReference<>() {});
            List<SubTaskSpec> specs = new ArrayList<>();
            for (Map<String, Object> item : items) {
                String title = str(item, "title");
                if (title == null || title.isBlank()) continue;
                specs.add(new SubTaskSpec(
                        title,
                        str(item, "description"),
                        difficulty(str(item, "difficulty")),
                        minutes(item.get("estimatedMinutes"))
                ));
            }
            return specs.isEmpty() ? null : specs;
        } catch (Exception e) {
            log.debug("[task-decomposer] JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start >= 0 && end > start) return raw.substring(start, end + 1);
        return raw.trim();
    }

    // ── Rule-based fallback ───────────────────────────────────────────────────

    private List<SubTaskSpec> decomposeThroughRules(AgentTask parent) {
        return switch (parent.getTaskType()) {
            case "RESUME" -> List.of(
                    new SubTaskSpec("Review and update work experience section",
                            "Focus on measurable achievements and action verbs.", "MEDIUM", 20),
                    new SubTaskSpec("Align keywords with target job description",
                            "Compare resume keywords against 1-2 job postings.", "EASY", 15),
                    new SubTaskSpec("Request AI resume diagnosis",
                            "Use the AI resume module for a score and suggestions.", "EASY", 10)
            );
            case "INTERVIEW" -> List.of(
                    new SubTaskSpec("Practice one STAR-format behavioral answer",
                            "Choose a common question and write out S-T-A-R structure.", "MEDIUM", 20),
                    new SubTaskSpec("Review feedback from last mock interview",
                            "Read the report and identify one weak dimension to improve.", "EASY", 15),
                    new SubTaskSpec("Do one 10-minute mock interview session",
                            "Start a focused mock with the interview module.", "HARD", 30)
            );
            case "ASSESSMENT" -> List.of(
                    new SubTaskSpec("Complete the career personality assessment",
                            "Answer all questions honestly in one sitting.", "EASY", 20),
                    new SubTaskSpec("Review assessment results and suggested roles",
                            "Read the report and note your top 2 recommended directions.", "EASY", 10)
            );
            case "LEARNING", "PLAN" -> List.of(
                    new SubTaskSpec("Break goal into one 30-minute focused study block",
                            "Pick one topic from the plan and study without distractions.", "MEDIUM", 30),
                    new SubTaskSpec("Write a 3-sentence reflection on today's progress",
                            "Note what you learned and what to do next.", "EASY", 10)
            );
            default -> List.of(
                    new SubTaskSpec("Complete first half of this task",
                            "Focus on the most impactful part first.", "MEDIUM", 20),
                    new SubTaskSpec("Review and finish remaining steps",
                            "Wrap up and verify the outcome.", "EASY", 15)
            );
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof String s ? s.trim() : null;
    }

    private String difficulty(String raw) {
        if ("EASY".equalsIgnoreCase(raw)) return "EASY";
        if ("HARD".equalsIgnoreCase(raw)) return "HARD";
        return "MEDIUM";
    }

    private int minutes(Object raw) {
        if (raw instanceof Number n) {
            int v = n.intValue();
            for (int bucket : new int[]{10, 15, 20, 30, 60}) {
                if (v <= bucket) return bucket;
            }
        }
        return 20;
    }

    record SubTaskSpec(String title, String description, String difficulty, int estimatedMinutes) {}
}
