package com.group1.career.controller;

import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.CheckIn;
import com.group1.career.repository.CheckInRepository;
import com.group1.career.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckInController.class)
public class CheckInControllerTest {

    private static final Long TEST_UID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CheckInService checkInService;

    @MockitoBean
    private CheckInRepository repo;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenAnswer(inv -> {
            HttpServletRequest req = inv.getArgument(0);
            req.setAttribute("userId", TEST_UID);
            return true;
        });
    }

    @Test
    @DisplayName("GET /api/checkin/status — returns check-in status for current user")
    public void testStatus() throws Exception {
        CheckInService.CheckInStatus status = CheckInService.CheckInStatus.builder()
                .todayCompleted(2)
                .todayTotal(3)
                .weeklyDays(4)
                .streakDays(3)
                .badgeEarnedThisWeek(false)
                .completedActionsToday(List.of("ASSESSMENT", "INTERVIEW"))
                .build();
        when(checkInService.getStatus(TEST_UID)).thenReturn(status);

        mockMvc.perform(get("/api/checkin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todayCompleted").value(2))
                .andExpect(jsonPath("$.data.todayTotal").value(3))
                .andExpect(jsonPath("$.data.streakDays").value(3))
                .andExpect(jsonPath("$.data.weeklyDays").value(4))
                .andExpect(jsonPath("$.data.badgeEarnedThisWeek").value(false));
    }

    @Test
    @DisplayName("GET /api/checkin/status — returns zero-filled status for new user")
    public void testStatus_NewUser() throws Exception {
        when(checkInService.getStatus(TEST_UID)).thenReturn(CheckInService.CheckInStatus.empty());

        mockMvc.perform(get("/api/checkin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCompleted").value(0))
                .andExpect(jsonPath("$.data.streakDays").value(0))
                .andExpect(jsonPath("$.data.badgeEarnedThisWeek").value(false));
    }

    @Test
    @DisplayName("POST /api/checkin/trigger — records action and returns updated status")
    public void testTrigger() throws Exception {
        CheckInService.CheckInStatus updatedStatus = CheckInService.CheckInStatus.builder()
                .todayCompleted(1)
                .todayTotal(3)
                .weeklyDays(1)
                .streakDays(1)
                .badgeEarnedThisWeek(false)
                .completedActionsToday(List.of("ASSESSMENT"))
                .build();
        doNothing().when(checkInService).recordAction(TEST_UID, "ASSESSMENT");
        when(checkInService.getStatus(TEST_UID)).thenReturn(updatedStatus);

        mockMvc.perform(post("/api/checkin/trigger")
                        .param("action", "ASSESSMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todayCompleted").value(1))
                .andExpect(jsonPath("$.data.streakDays").value(1));

        verify(checkInService).recordAction(TEST_UID, "ASSESSMENT");
        verify(checkInService).getStatus(TEST_UID);
    }

    @Test
    @DisplayName("POST /api/checkin/trigger — INTERVIEW action updates status")
    public void testTrigger_Interview() throws Exception {
        CheckInService.CheckInStatus updatedStatus = CheckInService.CheckInStatus.builder()
                .todayCompleted(1)
                .todayTotal(3)
                .weeklyDays(1)
                .streakDays(1)
                .badgeEarnedThisWeek(false)
                .completedActionsToday(List.of("INTERVIEW"))
                .build();
        doNothing().when(checkInService).recordAction(TEST_UID, "INTERVIEW");
        when(checkInService.getStatus(TEST_UID)).thenReturn(updatedStatus);

        mockMvc.perform(post("/api/checkin/trigger")
                        .param("action", "INTERVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayCompleted").value(1));

        verify(checkInService).recordAction(TEST_UID, "INTERVIEW");
    }

    @Test
    @DisplayName("GET /api/checkin/calendar — returns check-in rows for last 30 days")
    public void testCalendar() throws Exception {
        LocalDate today = LocalDate.now();
        List<CheckIn> checkIns = List.of(
                CheckIn.builder().id(1L).userId(TEST_UID).day(today).action("ASSESSMENT").build(),
                CheckIn.builder().id(2L).userId(TEST_UID).day(today.minusDays(1)).action("INTERVIEW").build()
        );
        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(TEST_UID), any(LocalDate.class), eq(today)))
                .thenReturn(checkIns);

        mockMvc.perform(get("/api/checkin/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].action").value("ASSESSMENT"))
                .andExpect(jsonPath("$.data[1].action").value("INTERVIEW"));
    }

    @Test
    @DisplayName("GET /api/checkin/calendar — returns empty list when no check-ins")
    public void testCalendar_Empty() throws Exception {
        when(repo.findByUserIdAndDayBetweenOrderByDayAsc(eq(TEST_UID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/checkin/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
