package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.CareerPath;
import com.group1.career.repository.CareerPathRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ListCareerPathsTool implements AiTool {

    private final CareerPathRepository careerPathRepository;

    @Override public String getName() { return "list_career_paths"; }
    @Override public String getDescription() {
        return "List all available career paths in the platform (e.g. Backend Engineer, Product Manager, Data Analyst).";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        List<CareerPath> paths = careerPathRepository.findAll();
        if (paths.isEmpty()) return "No career paths are currently available.";
        StringBuilder sb = new StringBuilder("Available career paths:\n");
        for (CareerPath p : paths) {
            sb.append("- ").append(p.getName());
            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                String desc = p.getDescription().length() > 80
                        ? p.getDescription().substring(0, 80) + "..." : p.getDescription();
                sb.append(": ").append(desc);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
