package com.group1.career.service;

import com.group1.career.model.entity.CheckIn;
import com.group1.career.repository.CheckInRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckInServiceTest {

    @Mock
    private CheckInRepository repo;

    @InjectMocks
    private CheckInService checkInService;

    // ========== recordAction ==========

    @Test
    @DisplayName("recordAction — saves new check-in for ASSESSMENT")
    public void testRecordAction_Saves() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(repo.findByUserIdAndDayAndAction(userId, today, "ASSESSMENT"))
                .thenReturn(Optional.empty());

        checkInService.recordAction(userId, "ASSESSMENT");

        verify(repo).save(argThat(c ->
                c.getUserId().equals(userId) &&
                "ASSESSMENT".equals(c.getAction()) &&
                c.getDay().equals(today)
        ));
    }

    @Test
    @DisplayName("recordAction — saves new check-in for INTERVIEW")
    public void testRecordAction_Interview() {
        Long userId = 2L;
        LocalDate today = LocalDate.now();
        when(repo.findByUserIdAndDayAndAction(userId, today, "INTERVIEW"))
                .thenReturn(Optional.empty());

        checkInService.recordAction(userId, "INTERVIEW");

        verify(repo).save(any(CheckIn.class));
    }

    @Test
    @DisplayName("recordAction — saves new check-in for SKILL_NODE")
    public void testRecordAction_SkillNode() {
        Long userId = 3L;
        LocalDate today = LocalDate.now();
        when(repo.findByUserIdAndDayAndAction(userId, today, "SKILL_NODE"))
                .thenReturn(Optional.empty());

        checkInService.recordAction(userId, "SKILL_NODE");

        verify(repo).save(any(CheckIn.class));
    }

    @Test
    @DisplayName("recordAction — idempotent when already recorded today")
    public void testRecordAction_Idempotent() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(repo.findByUserIdAndDayAndAction(userId, today, "INTERVIEW"))
                .thenReturn(Optional.of(CheckIn.builder().build()));

        checkInService.recordAction(userId, "INTERVIEW");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("recordAction — ignores unknown action silently")
    public void testRecordAction_UnknownAction() {
        checkInService.recordAction(1L, "UNKNOWN_ACTION");

        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("recordAction — ignores null userId")
    public void testRecordAction_NullUserId() {
        checkInService.recordAction(null, "ASSESSMENT");

        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("recordAction — ignores zero userId")
    public void testRecordAction_ZeroUserId() {
        checkInService.recordAction(0L, "ASSESSMENT");

        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("recordAction — ignores blank action")
    public void testRecordAction_BlankAction() {
        checkInService.recordAction(1L, "   ");

        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("recordAction — normalises action to uppercase before lookup")
    public void testRecordAction_NormalisesCase() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        when(repo.findByUserIdAndDayAndAction(userId, today, "ASSESSMENT"))
                .thenReturn(Optional.empty());

        checkInService.recordAction(userId, "assessment");

        verify(repo).findByUserIdAndDayAndAction(userId, today, "ASSESSMENT");
    }

    // ========== getStatus ==========

    @Test
    @DisplayName("getStatus — returns empty status for null userId")
    public void testGetStatus_NullUserId() {
        CheckInService.CheckInStatus status = checkInService.getStatus(null);

        assertNotNull(status);
        assertEquals(0, status.getTodayCompleted());
        assertEquals(CheckInService.CORE_ACTIONS.size(), status.getTodayTotal());
        assertEquals(0, status.getStreakDays());
        assertEquals(0, status.getWeeklyDays());
        assertFalse(status.isBadgeEarnedThisWeek());
        assertTrue(status.getCompletedActionsToday().isEmpty());
    }

    @Test
    @DisplayName("getStatus — returns empty status for non-positive userId")
    public void testGetStatus_NonPositiveUserId() {
        CheckInService.CheckInStatus status = checkInService.getStatus(-1L);

        assertNotNull(status);
        assertEquals(0, status.getTodayCompleted());
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("getStatus — todayCompleted reflects today's core actions done")
    public void testGetStatus_TodayCompleted() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        List<CheckIn> last7 = List.of(
                CheckIn.builder().userId(userId).day(today).action("ASSESSMENT").build(),
                CheckIn.builder().userId(userId).day(today).action("INTERVIEW").build()
        );

        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(last7);
        when(repo.distinctDaysInRange(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(List.of(today));

        CheckInService.CheckInStatus status = checkInService.getStatus(userId);

        assertEquals(2, status.getTodayCompleted());
        assertTrue(status.getCompletedActionsToday().contains("ASSESSMENT"));
        assertTrue(status.getCompletedActionsToday().contains("INTERVIEW"));
    }

    @Test
    @DisplayName("getStatus — streak counts consecutive days ending today")
    public void testGetStatus_ConsecutiveStreak() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        List<LocalDate> distinctDays = List.of(
                today, today.minusDays(1), today.minusDays(2)
        );

        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(Collections.emptyList());
        when(repo.distinctDaysInRange(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(distinctDays);

        CheckInService.CheckInStatus status = checkInService.getStatus(userId);

        assertEquals(3, status.getStreakDays());
    }

    @Test
    @DisplayName("getStatus — streak breaks on gap")
    public void testGetStatus_StreakBreaksOnGap() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        // today and day before yesterday — day in between is missing
        List<LocalDate> distinctDays = List.of(today, today.minusDays(2));

        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(Collections.emptyList());
        when(repo.distinctDaysInRange(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(distinctDays);

        CheckInService.CheckInStatus status = checkInService.getStatus(userId);

        // Only today counts as streak before the gap
        assertEquals(1, status.getStreakDays());
    }

    @Test
    @DisplayName("getStatus — badgeEarnedThisWeek true when 5 or more weekly days")
    public void testGetStatus_BadgeEarned() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        // 5 distinct days within the last 7 (including today)
        List<CheckIn> last7 = List.of(
                CheckIn.builder().userId(userId).day(today).action("ASSESSMENT").build(),
                CheckIn.builder().userId(userId).day(today.minusDays(1)).action("INTERVIEW").build(),
                CheckIn.builder().userId(userId).day(today.minusDays(2)).action("SKILL_NODE").build(),
                CheckIn.builder().userId(userId).day(today.minusDays(3)).action("ASSESSMENT").build(),
                CheckIn.builder().userId(userId).day(today.minusDays(4)).action("INTERVIEW").build()
        );

        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(last7);
        when(repo.distinctDaysInRange(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(List.of(today, today.minusDays(1), today.minusDays(2),
                        today.minusDays(3), today.minusDays(4)));

        CheckInService.CheckInStatus status = checkInService.getStatus(userId);

        assertTrue(status.isBadgeEarnedThisWeek());
        assertEquals(5, status.getWeeklyDays());
    }

    @Test
    @DisplayName("getStatus — streak is 0 when no check-in today")
    public void testGetStatus_NoCheckInToday() {
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        // Only a check-in two days ago — streak should be 0 (gap at yesterday)
        List<LocalDate> distinctDays = List.of(today.minusDays(2));

        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(Collections.emptyList());
        when(repo.distinctDaysInRange(eq(userId), any(LocalDate.class), eq(today)))
                .thenReturn(distinctDays);

        CheckInService.CheckInStatus status = checkInService.getStatus(userId);

        assertEquals(0, status.getStreakDays());
    }
}
