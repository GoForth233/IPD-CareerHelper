package com.group1.career.service.ai.tools.impl;

import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.repository.InterviewQuestionRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchQuestionBankTool implements AiTool {

    private final InterviewQuestionRepository questionRepository;

    @Override public String getName() { return "search_question_bank"; }
    @Override public String getDescription() {
        return "Search the interview question bank by keyword. Returns matching questions with difficulty and category.";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "keyword", Map.of("type", "string", "description", "Search keyword for position or topic"),
                        "limit", Map.of("type", "integer", "description", "Max results (1-10, default 5)")
                ),
                "required", new String[]{"keyword"}
        );
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        String keyword = args.containsKey("keyword") ? String.valueOf(args.get("keyword")) : "";
        int limit = 5;
        if (args.containsKey("limit")) {
            try { limit = Math.min(10, Math.max(1, ((Number) args.get("limit")).intValue())); }
            catch (Exception ignored) {}
        }
        Page<InterviewQuestion> page = questionRepository.search(
                keyword.isBlank() ? null : keyword,
                null,
                null,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "likes")));
        if (page.isEmpty()) return "No questions found matching keyword: " + keyword;
        StringBuilder sb = new StringBuilder("Question bank results for \"" + keyword + "\":\n");
        for (InterviewQuestion q : page.getContent()) {
            sb.append("- [").append(q.getDifficulty()).append("] ");
            String content = q.getContent();
            if (content != null && content.length() > 120) content = content.substring(0, 120) + "...";
            sb.append(content).append("\n");
        }
        return sb.toString();
    }
}
