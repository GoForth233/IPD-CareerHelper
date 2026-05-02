package com.group1.career.service.ai.tools.impl;

import com.group1.career.repository.CheckInRepository;
import com.group1.career.service.ai.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetStreakStatusTool implements AiTool {

    private final CheckInRepository checkInRepository;

    @Override public String getName() { return "get_streak_status"; }
    @Override public String getDescription() {
        return "Get the user's current daily check-in streak and total check-in days in the last 30 days.";
    }
    @Override public Map<String, Object> getParameterSchema() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public String execute(Map<String, Object> args, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        List<LocalDate> days = checkInRepository.distinctDaysInRange(userId, thirtyDaysAgo, today);
        if (days.isEmpty()) return "The user has not checked in during the last 30 days.";

        int streak = 0;
        LocalDate cursor = today;
        for (LocalDate d : days) {
            if (d.equals(cursor) || d.equals(cursor.minusDays(1))) {
                streak++;
                cursor = d;
            } else {
                break;
            }
        }
        return String.format("Check-in streak: %d consecutive days. Total check-in days in last 30 days: %d.",
                streak, days.size());
    }
}
