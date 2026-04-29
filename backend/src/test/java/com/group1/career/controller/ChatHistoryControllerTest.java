package com.group1.career.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.document.ChatSessionDocument;
import com.group1.career.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatHistoryController.class)
public class ChatHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatSessionRepository chatSessionRepository;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/chat/history/{userId} - Returns sessions sorted by updated")
    public void testGetUserSessions_Success() throws Exception {
        Long userId = 1L;
        List<ChatSessionDocument> sessions = Arrays.asList(
                ChatSessionDocument.builder().id("session_2").userId(userId).title("Second Chat").updatedAt(new Date()).build(),
                ChatSessionDocument.builder().id("session_1").userId(userId).title("First Chat").updatedAt(new Date()).build()
        );
        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(sessions);

        mockMvc.perform(get("/api/chat/history/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("session_2"))
                .andExpect(jsonPath("$.data[1].id").value("session_1"));
    }

    @Test
    @DisplayName("GET /api/chat/history/{userId} - Returns empty list when no sessions")
    public void testGetUserSessions_Empty() throws Exception {
        Long userId = 99L;
        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/chat/history/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/chat/history/session/{sessionId} - Returns specific session")
    public void testGetSession_Success() throws Exception {
        String sessionId = "session_abc";
        ChatSessionDocument session = ChatSessionDocument.builder()
                .id(sessionId).userId(1L).title("My Chat").messages(new ArrayList<>()).build();
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        mockMvc.perform(get("/api/chat/history/session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(sessionId))
                .andExpect(jsonPath("$.data.title").value("My Chat"));
    }

    @Test
    @DisplayName("GET /api/chat/history/session/{sessionId} - Returns error when session not found")
    public void testGetSession_NotFound() throws Exception {
        String sessionId = "nonexistent";
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/chat/history/session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("POST /api/chat/history/create - Creates session with custom title")
    public void testCreateSession_WithTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 1);
        request.put("title", "Career Questions");

        ChatSessionDocument saved = ChatSessionDocument.builder()
                .id("new_session_1").userId(1L).title("Career Questions")
                .messages(new ArrayList<>()).createdAt(new Date()).updatedAt(new Date()).build();
        when(chatSessionRepository.save(any(ChatSessionDocument.class))).thenReturn(saved);

        mockMvc.perform(post("/api/chat/history/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("new_session_1"))
                .andExpect(jsonPath("$.data.title").value("Career Questions"));
    }

    @Test
    @DisplayName("POST /api/chat/history/create - Uses default title when not provided")
    public void testCreateSession_DefaultTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2);

        ChatSessionDocument saved = ChatSessionDocument.builder()
                .id("new_session_2").userId(2L).title("New Conversation").messages(new ArrayList<>()).build();
        when(chatSessionRepository.save(any(ChatSessionDocument.class))).thenReturn(saved);

        mockMvc.perform(post("/api/chat/history/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("New Conversation"));
    }

    @Test
    @DisplayName("POST /api/chat/history/session/{sessionId}/append - Appends messages to session")
    public void testAppendMessage_Success() throws Exception {
        String sessionId = "session_abc";
        Map<String, String> request = new HashMap<>();
        request.put("userMessage", "What is Spring Boot?");
        request.put("assistantReply", "Spring Boot is a framework...");

        ChatSessionDocument existingSession = ChatSessionDocument.builder()
                .id(sessionId).userId(1L).title("New Conversation")
                .messages(new ArrayList<>()).updatedAt(new Date()).build();
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(chatSessionRepository.save(any(ChatSessionDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/chat/history/session/{sessionId}/append", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.messages.length()").value(2))
                .andExpect(jsonPath("$.data.messages[0].role").value("user"))
                .andExpect(jsonPath("$.data.messages[1].role").value("assistant"));
    }

    @Test
    @DisplayName("POST /api/chat/history/session/{sessionId}/append - Auto-generates title from first message")
    public void testAppendMessage_AutoTitle() throws Exception {
        String sessionId = "session_auto";
        Map<String, String> request = new HashMap<>();
        request.put("userMessage", "Tell me about Java programming");
        request.put("assistantReply", "Java is a versatile language...");

        ChatSessionDocument existingSession = ChatSessionDocument.builder()
                .id(sessionId).userId(1L).title("New Conversation")
                .messages(new ArrayList<>()).updatedAt(new Date()).build();
        when(chatSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(chatSessionRepository.save(any(ChatSessionDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/chat/history/session/{sessionId}/append", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Tell me about Java p..."));
    }

    @Test
    @DisplayName("DELETE /api/chat/history/session/{sessionId} - Deletes session")
    public void testDeleteSession_Success() throws Exception {
        String sessionId = "session_to_delete";
        doNothing().when(chatSessionRepository).deleteById(sessionId);

        mockMvc.perform(delete("/api/chat/history/session/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Session deleted"));

        verify(chatSessionRepository).deleteById(sessionId);
    }
}
