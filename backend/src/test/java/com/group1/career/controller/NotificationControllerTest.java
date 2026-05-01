package com.group1.career.controller;

import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.Notification;
import com.group1.career.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    private static final Long TEST_UID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

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
    @DisplayName("GET /api/notifications — returns list of notifications newest first")
    public void testList() throws Exception {
        List<Notification> notifications = List.of(
                Notification.builder().notificationId(2L).userId(TEST_UID)
                        .type("INTERVIEW_COMPLETED").title("Interview done").readFlag(false).build(),
                Notification.builder().notificationId(1L).userId(TEST_UID)
                        .type("ASSESSMENT_DONE").title("MBTI done").readFlag(true).build()
        );
        when(notificationService.listForUser(TEST_UID)).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].type").value("INTERVIEW_COMPLETED"))
                .andExpect(jsonPath("$.data[1].type").value("ASSESSMENT_DONE"));
    }

    @Test
    @DisplayName("GET /api/notifications — returns empty list when no notifications")
    public void testList_Empty() throws Exception {
        when(notificationService.listForUser(TEST_UID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count — returns count map")
    public void testUnreadCount() throws Exception {
        when(notificationService.unreadCount(TEST_UID)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.count").value(3));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count — returns 0 when all read")
    public void testUnreadCount_Zero() throws Exception {
        when(notificationService.unreadCount(TEST_UID)).thenReturn(0L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read — marks notification as read")
    public void testMarkRead() throws Exception {
        doNothing().when(notificationService).markRead(10L, TEST_UID);

        mockMvc.perform(post("/api/notifications/{notificationId}/read", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("ok"));

        verify(notificationService).markRead(10L, TEST_UID);
    }

    @Test
    @DisplayName("POST /api/notifications/read-all — marks all notifications as read")
    public void testMarkAllRead() throws Exception {
        when(notificationService.markAllRead(TEST_UID)).thenReturn(5);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.updated").value(5));
    }

    @Test
    @DisplayName("POST /api/notifications/read-all — returns 0 updated when nothing to mark")
    public void testMarkAllRead_NothingToMark() throws Exception {
        when(notificationService.markAllRead(TEST_UID)).thenReturn(0);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(0));
    }
}
