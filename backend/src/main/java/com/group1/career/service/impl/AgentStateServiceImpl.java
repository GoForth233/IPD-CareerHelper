package com.group1.career.service.impl;

import com.group1.career.model.entity.AgentEvent;
import com.group1.career.model.entity.AgentState;
import com.group1.career.model.entity.AgentTask;
import com.group1.career.repository.AgentEventRepository;
import com.group1.career.repository.AgentStateRepository;
import com.group1.career.repository.AgentTaskRepository;
import com.group1.career.repository.AgentUserProfileRepository;
import com.group1.career.service.AgentStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentStateServiceImpl implements AgentStateService {

    private final AgentStateRepository stateRepository;
    private final AgentTaskRepository taskRepository;
    private final AgentEventRepository eventRepository;
    private final AgentUserProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public AgentState getOrCreate(Long userId) {
        return stateRepository.findByUserId(userId)
                .orElseGet(() -> AgentState.builder().userId(userId).build());
    }

    @Override
    @Transactional
    public AgentState refresh(Long userId, String currentStage, String primaryRiskCode,
                              String primaryGoal, String preferredDifficulty) {
        List<AgentTask> recent = taskRepository.findByUserIdAndDueDateBetweenOrderByDueDateDescCreatedAtDesc(
                userId, LocalDate.now().minusDays(6), LocalDate.now());

        int total = recent.size();
        long done = recent.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        long dismissed = recent.stream().filter(t -> "DISMISSED".equals(t.getStatus())).count();

        BigDecimal completionRate = total > 0
                ? BigDecimal.valueOf(done).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal dismissRate = total > 0
                ? BigDecimal.valueOf(dismissed).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String resolvedRiskCode = primaryRiskCode != null ? primaryRiskCode
                : profileRepository.findByUserId(userId)
                        .map(p -> p.getPrimaryRiskCode()).orElse(null);

        AgentState state = stateRepository.findByUserId(userId)
                .orElseGet(() -> AgentState.builder().userId(userId).build());

        state.setCurrentStage(currentStage);
        state.setPrimaryRiskCode(resolvedRiskCode);
        state.setPrimaryGoal(primaryGoal);
        state.setTaskCompletionRate7d(completionRate);
        state.setTaskDismissRate7d(dismissRate);
        state.setPreferredTaskDifficulty(
                preferredDifficulty != null ? preferredDifficulty : "MEDIUM");
        state.setLastActiveAt(LocalDateTime.now());

        return stateRepository.save(state);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AgentEvent> getLatestWeeklyReview(Long userId) {
        return eventRepository.findTopByUserIdAndEventTypeOrderByCreatedAtDesc(
                userId, "WEEKLY_REVIEW_COMPLETED");
    }
}
