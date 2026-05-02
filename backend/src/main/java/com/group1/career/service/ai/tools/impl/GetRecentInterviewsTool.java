package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.Interview;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetRecentInterviewsTool implements AiTool {

    private final InterviewRepository interviewRepository;

    @Override public String getName() { return "get_recent_interviews"; }
    @Override public String getDescription() {
        return "Get the user's recent mock interview records including position, score and difficulty. Optionally specify n (default 3, max 5).";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "n", Map.of("type", "integer", "description", "Number of interviews to return (1-5, default 3)")
                )
        );
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        int n = 3;
        if (args.containsKey("n")) {
            try { n = Math.min(5, Math.max(1, ((Number) args.get("n")).intValue())); }
            catch (Exception ignored) {}
        }
        List<Interview> all = interviewRepository.findByUserIdOrderByStartedAtDesc(userId);
        if (all.isEmpty()) return "The user has not completed any mock interviews yet.";
        List<Interview> recent = all.subList(0, Math.min(n, all.size()));
        StringBuilder sb = new StringBuilder("Recent mock interviews:\n");
        for (Interview iv : recent) {
            sb.append("- Position: ").append(iv.getPositionName())
              .append(", Difficulty: ").append(iv.getDifficulty())
              .append(", Status: ").append(iv.getStatus());
            if (iv.getFinalScore() != null) sb.append(", Score: ").append(iv.getFinalScore());
            sb.append(", Date: ").append(iv.getStartedAt()).append("\n");
        }
        return sb.toString();
    }
}
