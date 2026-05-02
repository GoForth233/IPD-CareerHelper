package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.model.entity.UserFact;
import com.group1.career.repository.InterviewQuestionRepository;
import com.group1.career.repository.UserFactRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RecommendQuestionsTool implements AiTool {

    private final InterviewQuestionRepository questionRepository;
    private final UserFactRepository factRepository;

    @Override public String getName() { return "recommend_questions"; }
    @Override public String getDescription() {
        return "Recommend personalised interview practice questions based on the user's target role and past facts. Optionally specify count (default 3, max 5).";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "count", Map.of("type", "integer", "description", "Number of questions (1-5, default 3)")
                )
        );
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        int count = 3;
        if (args.containsKey("count")) {
            try { count = Math.min(5, Math.max(1, ((Number) args.get("count")).intValue())); }
            catch (Exception ignored) {}
        }

        String position = null;
        List<UserFact> facts = factRepository.findByUserIdAndCategory(userId, "CAREER_GOAL");
        for (UserFact f : facts) {
            if ("target_role".equals(f.getFactKey()) || "target_position".equals(f.getFactKey())) {
                position = f.getFactValue();
                break;
            }
        }

        List<InterviewQuestion> pool = questionRepository.drawPool(
                position != null ? position : "Backend",
                "Normal",
                PageRequest.of(0, count));

        if (pool.isEmpty()) {
            pool = questionRepository.search(null, null, null, PageRequest.of(0, count)).getContent();
        }
        if (pool.isEmpty()) return "No questions available at the moment.";

        StringBuilder sb = new StringBuilder("Recommended practice questions");
        if (position != null) sb.append(" for ").append(position);
        sb.append(":\n");
        for (InterviewQuestion q : pool) {
            String content = q.getContent();
            if (content != null && content.length() > 150) content = content.substring(0, 150) + "...";
            sb.append("- [").append(q.getDifficulty()).append("] ").append(content).append("\n");
        }
        return sb.toString();
    }
}
