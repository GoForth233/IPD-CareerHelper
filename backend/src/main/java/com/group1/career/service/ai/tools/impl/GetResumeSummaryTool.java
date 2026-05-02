package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.Resume;
import com.group1.career.repository.ResumeRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetResumeSummaryTool implements AiTool {

    private final ResumeRepository resumeRepository;

    @Override public String getName() { return "get_resume_summary"; }
    @Override public String getDescription() {
        return "Get a summary of the user's current resume: title, target job, diagnosis score, and parsed skills if available.";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        List<Resume> resumes = resumeRepository.findByUserId(userId);
        if (resumes.isEmpty()) return "The user has not uploaded a resume yet.";
        Resume r = resumes.get(resumes.size() - 1); // most recently created (ascending order)
        StringBuilder sb = new StringBuilder("Resume summary:\n");
        if (r.getTitle() != null) sb.append("Title: ").append(r.getTitle()).append("\n");
        if (r.getTargetJob() != null) sb.append("Target job: ").append(r.getTargetJob()).append("\n");
        sb.append("Status: ").append(r.getStatus()).append("\n");
        if (r.getDiagnosisScore() != null && r.getDiagnosisScore() > 0) {
            sb.append("Diagnosis score: ").append(r.getDiagnosisScore()).append("/100\n");
        }
        return sb.toString();
    }
}
