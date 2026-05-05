package com.group1.career.job;

import com.group1.career.model.entity.AgentState;
import com.group1.career.model.entity.User;
import com.group1.career.repository.AgentStateRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.AgentEventService;
import com.group1.career.service.CareerPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase E4: weekly review job.
 *
 * <p>Runs every Sunday at 19:00 (Asia/Shanghai). For every active user that
 * has a state row, it:
 * <ol>
 *   <li>Builds a summary payload from {@code agent_states}.</li>
 *   <li>Writes a {@code WEEKLY_REVIEW_COMPLETED} event via AgentEventService.</li>
 *   <li>Updates {@code agent_states.last_weekly_review_at}.</li>
 *   <li>Triggers an async career-plan refresh so weekly_focus stays current.</li>
 * </ol>
 * Safe to re-run: each run produces a new event row, but plan regeneration is
 * idempotent. The frontend shows only the latest review.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentWeeklyReviewJob {

    private final UserRepository userRepository;
    private final AgentStateRepository stateRepository;
    private final AgentEventService agentEventService;
    private final CareerPlanService careerPlanService;

    @Scheduled(cron = "0 0 19 ? * SUN", zone = "Asia/Shanghai")
    public void run() {
        log.info("[agent-weekly-review] cron tick starting");
        LocalDate weekEnd = LocalDate.now();
        LocalDate weekStart = weekEnd.minusDays(6);

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null
                        && (u.getStatus() == null || u.getStatus() == 1))
                .toList();

        int processed = 0;
        int skipped = 0;

        for (User user : activeUsers) {
            try {
                AgentState state = stateRepository.findByUserId(user.getUserId()).orElse(null);
                if (state == null) {
                    skipped++;
                    continue;
                }
                processUser(user.getUserId(), state, weekStart, weekEnd);
                processed++;
            } catch (Exception e) {
                log.error("[agent-weekly-review] failed for user={}: {}", user.getUserId(), e.getMessage());
            }
        }

        log.info("[agent-weekly-review] done — processed={} skipped={}", processed, skipped);
    }

    @Transactional
    protected void processUser(Long userId, AgentState state, LocalDate weekStart, LocalDate weekEnd) {
        Map<String, Object> payload = buildPayload(state, weekStart, weekEnd);
        agentEventService.record(userId, "WEEKLY_REVIEW_COMPLETED", "SCHEDULER", payload);

        state.setLastWeeklyReviewAt(LocalDateTime.now());
        stateRepository.save(state);

        careerPlanService.regenerateAsync(userId);
    }

    private Map<String, Object> buildPayload(AgentState state, LocalDate weekStart, LocalDate weekEnd) {
        double completionRate = state.getTaskCompletionRate7d() != null
                ? state.getTaskCompletionRate7d().doubleValue() : 0.0;
        double dismissRate = state.getTaskDismissRate7d() != null
                ? state.getTaskDismissRate7d().doubleValue() : 0.0;

        List<String> highlights = new ArrayList<>();
        if (completionRate >= 0.6) highlights.add("Completed over 60% of agent tasks this week");
        if (dismissRate < 0.2) highlights.add("Task dismiss rate is healthy (< 20%)");
        if (state.getCurrentStage() != null) {
            highlights.add("Current focus stage: " + state.getCurrentStage());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("weekStart", weekStart.toString());
        payload.put("weekEnd", weekEnd.toString());
        payload.put("completionRate7d", completionRate);
        payload.put("dismissRate7d", dismissRate);
        payload.put("currentStage", state.getCurrentStage());
        payload.put("primaryRiskCode", state.getPrimaryRiskCode());
        payload.put("preferredDifficulty", state.getPreferredTaskDifficulty());
        payload.put("highlights", highlights);
        return payload;
    }
}
