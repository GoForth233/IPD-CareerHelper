package com.group1.career.service;

import com.group1.career.model.entity.CheckIn;
import com.group1.career.repository.CheckInRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 7-day check-in: a small streak game that pulls users back daily.
 * <p>
 * The engine is dead-simple:
 *   - {@link #recordAction(Long, String)} stamps "this user did X today" and
 *     is idempotent on the (user, day, action) unique key.
 *   - {@link #getStatus(Long)} reads the last 7 calendar days and computes
 *     today's progress (1 of 3 core actions), the consecutive-day streak,
 *     and whether the user already earned the weekly badge.
 *
 * <p>Action codes accepted: {@code ASSESSMENT}, {@code INTERVIEW},
 * {@code SKILL_NODE}. Anything else is silently dropped — service-side
 * triggers passing typos shouldn't break the user-visible flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    /** Subset of actions that count toward "today's progress" (3 chips). */
    public static final List<String> CORE_ACTIONS = List.of("ASSESSMENT", "INTERVIEW", "SKILL_NODE");

    /** Streak window — we never bother walking further back than this. */
    private static final int STREAK_LOOKBACK_DAYS = 30;

    private final CheckInRepository repo;

    /**
     * Stamp a check-in for today. Idempotent per (user, day, action).
     */
    @Transactional
    public void recordAction(Long userId, String action) {
        if (userId == null || userId <= 0) return;
        if (action == null || action.isBlank()) return;
        String normalized = action.trim().toUpperCase();
        if (!CORE_ACTIONS.contains(normalized)) {
            log.debug("[checkin] ignoring non-core action '{}'", action);
            return;
        }
        LocalDate today = LocalDate.now();
        if (repo.findByUserIdAndDayAndAction(userId, today, normalized).isPresent()) return;
        try {
            repo.save(CheckIn.builder()
                    .userId(userId)
                    .day(today)
                    .action(normalized)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Concurrent submission landed first — the unique constraint did
            // its job, just keep going.
            log.debug("[checkin] race on (user={}, action={}, day={}) — already recorded",
                    userId, normalized, today);
        }
    }

    /**
     * Read the user's check-in status. Cheap (one DB read for last 7 days
     * + one for last 30 days). Safe to call on every home-page refresh.
     */
    @Transactional(readOnly = true)
    public CheckInStatus getStatus(Long userId) {
        if (userId == null || userId <= 0) {
            return CheckInStatus.empty();
        }
        LocalDate today = LocalDate.now();
        LocalDate sevenAgo = today.minusDays(6);   // [today-6 .. today] inclusive

        // 7-day rolling window — what fills the home-page chips.
        List<CheckIn> last7 = repo.findByUserIdAndDayBetweenOrderByDayAsc(userId, sevenAgo, today);

        boolean[] coreToday = new boolean[CORE_ACTIONS.size()];
        boolean[] daysCovered = new boolean[7];
        for (CheckIn c : last7) {
            int idx = (int) (c.getDay().toEpochDay() - sevenAgo.toEpochDay());
            if (idx >= 0 && idx < 7) daysCovered[idx] = true;
            if (c.getDay().equals(today)) {
                int act = CORE_ACTIONS.indexOf(c.getAction());
                if (act >= 0) coreToday[act] = true;
            }
        }
        int completedToday = 0;
        for (boolean b : coreToday) if (b) completedToday++;
        int weeklyDays = 0;
        for (boolean b : daysCovered) if (b) weeklyDays++;

        // Streak: walk back day-by-day from today until we hit a gap. Capped
        // at STREAK_LOOKBACK_DAYS so we never page through the whole table.
        LocalDate cutoff = today.minusDays(STREAK_LOOKBACK_DAYS - 1);
        List<LocalDate> distinctDays = repo.distinctDaysInRange(userId, cutoff, today);
        int streak = 0;
        LocalDate cursor = today;
        for (LocalDate d : distinctDays) {
            if (d.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (d.isBefore(cursor)) {
                break;
            }
        }

        return CheckInStatus.builder()
                .todayCompleted(completedToday)
                .todayTotal(CORE_ACTIONS.size())
                .weeklyDays(weeklyDays)
                .streakDays(streak)
                .badgeEarnedThisWeek(weeklyDays >= 5)
                .completedActionsToday(asActionList(coreToday))
                .build();
    }

    private List<String> asActionList(boolean[] flags) {
        List<String> out = new java.util.ArrayList<>();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) out.add(CORE_ACTIONS.get(i));
        }
        return out;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckInStatus {
        /** 0..3 — how many of today's core actions are done. */
        private int todayCompleted;
        private int todayTotal;
        /** 0..7 — distinct days with any check-in in the rolling 7-day window. */
        private int weeklyDays;
        /** Consecutive days with at least one check-in, ending today. */
        private int streakDays;
        /** Heuristic: at least 5/7 days = "weekly badge earned". */
        private boolean badgeEarnedThisWeek;
        /** Action codes already done today (for chip green state). */
        private List<String> completedActionsToday;

        public static CheckInStatus empty() {
            return CheckInStatus.builder()
                    .todayCompleted(0)
                    .todayTotal(CORE_ACTIONS.size())
                    .weeklyDays(0)
                    .streakDays(0)
                    .badgeEarnedThisWeek(false)
                    .completedActionsToday(List.of())
                    .build();
        }
    }
}
