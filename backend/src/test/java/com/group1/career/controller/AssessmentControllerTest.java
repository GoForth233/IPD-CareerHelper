package com.group1.career.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.*;
import com.group1.career.repository.AssessmentOptionRepository;
import com.group1.career.service.AssessmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssessmentController.class)
public class AssessmentControllerTest {

    private static final Long TEST_UID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentService assessmentService;

    @MockitoBean
    private AssessmentOptionRepository optionRepository;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenAnswer(inv -> {
            HttpServletRequest req = inv.getArgument(0);
            req.setAttribute("userId", TEST_UID);
            return true;
        });
    }

    @Test
    @DisplayName("GET /api/assessments/scales — returns list of active scales")
    public void testGetScales() throws Exception {
        List<AssessmentScale> scales = List.of(
                AssessmentScale.builder().scaleId(1L).title("MBTI").isActive(true).build(),
                AssessmentScale.builder().scaleId(2L).title("Holland Code").isActive(true).build()
        );
        when(assessmentService.getAllScales()).thenReturn(scales);

        mockMvc.perform(get("/api/assessments/scales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("MBTI"))
                .andExpect(jsonPath("$.data[1].title").value("Holland Code"));
    }

    @Test
    @DisplayName("GET /api/assessments/scales — returns empty list when no active scales")
    public void testGetScales_Empty() throws Exception {
        when(assessmentService.getAllScales()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/assessments/scales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/assessments/scales/{scaleId}/questions — returns questions with options")
    public void testGetQuestions() throws Exception {
        Long scaleId = 1L;
        List<AssessmentQuestion> questions = List.of(
                AssessmentQuestion.builder().questionId(10L).scaleId(scaleId)
                        .questionText("Are you more E or I?").questionType("SINGLE").sortOrder(1).build()
        );
        List<AssessmentOption> options = List.of(
                AssessmentOption.builder().optionId(100L).questionId(10L)
                        .optionLabel("A").optionText("Extrovert").build(),
                AssessmentOption.builder().optionId(101L).questionId(10L)
                        .optionLabel("B").optionText("Introvert").build()
        );
        when(assessmentService.getScaleQuestions(scaleId)).thenReturn(questions);
        when(optionRepository.findByQuestionIdIn(anyList())).thenReturn(options);

        mockMvc.perform(get("/api/assessments/scales/{scaleId}/questions", scaleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].questionText").value("Are you more E or I?"))
                .andExpect(jsonPath("$.data[0].options.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/assessments/submit — submits answers and returns record")
    public void testSubmit() throws Exception {
        AssessmentRecord record = AssessmentRecord.builder()
                .recordId(5L).userId(TEST_UID).scaleId(1L).status("COMPLETED")
                .resultSummary("INTJ").build();
        when(assessmentService.submitAndScore(eq(TEST_UID), eq(1L), anyMap()))
                .thenReturn(record);

        Map<String, Object> body = new HashMap<>();
        body.put("scaleId", 1);
        Map<String, Integer> answers = new HashMap<>();
        answers.put("10", 100);
        body.put("answers", answers);

        mockMvc.perform(post("/api/assessments/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recordId").value(5))
                .andExpect(jsonPath("$.data.resultSummary").value("INTJ"));
    }

    @Test
    @DisplayName("GET /api/assessments/records — returns user's assessment history")
    public void testGetMyRecords() throws Exception {
        List<AssessmentRecord> records = List.of(
                AssessmentRecord.builder().recordId(1L).userId(TEST_UID)
                        .scaleId(1L).status("COMPLETED").resultSummary("ENTJ").build()
        );
        when(assessmentService.getUserRecords(TEST_UID)).thenReturn(records);

        mockMvc.perform(get("/api/assessments/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].resultSummary").value("ENTJ"));
    }

    @Test
    @DisplayName("GET /api/assessments/records/detail/{recordId} — returns record when caller owns it")
    public void testGetRecord_OwnRecord() throws Exception {
        Long recordId = 7L;
        AssessmentRecord record = AssessmentRecord.builder()
                .recordId(recordId).userId(TEST_UID).status("COMPLETED").build();
        when(assessmentService.getRecord(recordId)).thenReturn(record);

        mockMvc.perform(get("/api/assessments/records/detail/{recordId}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recordId").value(recordId));
    }

    @Test
    @DisplayName("GET /api/assessments/records/detail/{recordId} — IDOR blocked when userId doesn't match")
    public void testGetRecord_IDOR() throws Exception {
        Long recordId = 7L;
        AssessmentRecord record = AssessmentRecord.builder()
                .recordId(recordId).userId(999L).status("COMPLETED").build();
        when(assessmentService.getRecord(recordId)).thenReturn(record);

        mockMvc.perform(get("/api/assessments/records/detail/{recordId}", recordId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
