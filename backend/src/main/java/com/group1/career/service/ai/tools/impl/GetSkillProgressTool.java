package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.UserCareerProgress;
import com.group1.career.repository.UserCareerProgressRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetSkillProgressTool implements AiTool {

    private final UserCareerProgressRepository progressRepository;

    @Override public String getName() { return "get_skill_progress"; }
    @Override public String getDescription() {
        return "Get the user's career path node progress. Shows which career path nodes are completed or in progress.";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        List<UserCareerProgress> progresses = progressRepository.findByUserId(userId);
        if (progresses.isEmpty()) return "The user has not started any career path learning nodes yet.";
        long completed = progresses.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long inProgress = progresses.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        return String.format("Career path progress: %d nodes completed, %d in progress, %d total tracked.",
                completed, inProgress, progresses.size());
    }
}
