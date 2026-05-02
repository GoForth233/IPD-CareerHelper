package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.AssessmentRecord;
import com.group1.career.repository.AssessmentRecordRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetRecentAssessmentTool implements AiTool {

    private final AssessmentRecordRepository recordRepository;

    @Override public String getName() { return "get_recent_assessment"; }
    @Override public String getDescription() {
        return "Get the user's most recent assessment result (e.g. MBTI, RIASEC). Returns the result summary and AI insight.";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        List<AssessmentRecord> records = recordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (records.isEmpty()) return "The user has not completed any assessments yet.";
        AssessmentRecord r = records.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("Most recent assessment (record #").append(r.getRecordId()).append("):\n");
        if (r.getResultSummary() != null) sb.append("Summary: ").append(r.getResultSummary()).append("\n");
        if (r.getAiInsight() != null) {
            String insight = r.getAiInsight().length() > 300
                    ? r.getAiInsight().substring(0, 300) + "..." : r.getAiInsight();
            sb.append("AI Insight: ").append(insight).append("\n");
        }
        sb.append("Completed at: ").append(r.getCreatedAt());
        return sb.toString();
    }
}
