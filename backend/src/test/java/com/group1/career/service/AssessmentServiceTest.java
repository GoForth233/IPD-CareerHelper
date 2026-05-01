package com.group1.career.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.*;
import com.group1.career.repository.*;
import com.group1.career.service.impl.AssessmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssessmentServiceTest {

    @Mock private AssessmentScaleRepository scaleRepository;
    @Mock private AssessmentQuestionRepository questionRepository;
    @Mock private AssessmentOptionRepository optionRepository;
    @Mock private AssessmentRecordRepository recordRepository;
    @Mock private AssessmentAnswerRepository answerRepository;
    @Spy  private ObjectMapper objectMapper;
    @Mock private AiService aiService;
    @Mock private NotificationService notificationService;
    @Mock private UserProfileSnapshotService snapshotService;
    @Mock private CheckInService checkInService;

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    @Test
    @DisplayName("getAllScales — delegates to repository and returns list")
    public void testGetAllScales() {
        List<AssessmentScale> scales = List.of(
                AssessmentScale.builder().scaleId(1L).title("MBTI").isActive(true).build(),
                AssessmentScale.builder().scaleId(2L).title("Holland").isActive(true).build()
        );
        when(scaleRepository.findByIsActiveTrueOrderByScaleIdAsc()).thenReturn(scales);

        List<AssessmentScale> result = assessmentService.getAllScales();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("MBTI", result.get(0).getTitle());
        assertEquals("Holland", result.get(1).getTitle());
        verify(scaleRepository).findByIsActiveTrueOrderByScaleIdAsc();
    }

    @Test
    @DisplayName("getScaleQuestions — returns active questions ordered by sortOrder")
    public void testGetScaleQuestions() {
        Long scaleId = 1L;
        List<AssessmentQuestion> questions = List.of(
                AssessmentQuestion.builder().questionId(1L).scaleId(scaleId)
                        .questionText("Are you more E or I?").sortOrder(1).build(),
                AssessmentQuestion.builder().questionId(2L).scaleId(scaleId)
                        .questionText("Are you more S or N?").sortOrder(2).build()
        );
        when(questionRepository.findByScaleIdAndIsActiveTrueOrderBySortOrderAsc(scaleId))
                .thenReturn(questions);

        List<AssessmentQuestion> result = assessmentService.getScaleQuestions(scaleId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Are you more E or I?", result.get(0).getQuestionText());
        verify(questionRepository).findByScaleIdAndIsActiveTrueOrderBySortOrderAsc(scaleId);
    }

    @Test
    @DisplayName("getUserRecords — returns records ordered by createdAt desc")
    public void testGetUserRecords() {
        Long userId = 42L;
        List<AssessmentRecord> records = List.of(
                AssessmentRecord.builder().recordId(1L).userId(userId).status("COMPLETED").build(),
                AssessmentRecord.builder().recordId(2L).userId(userId).status("COMPLETED").build()
        );
        when(recordRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(records);

        List<AssessmentRecord> result = assessmentService.getUserRecords(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recordRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getRecord — returns record when found")
    public void testGetRecord_Found() {
        Long recordId = 10L;
        AssessmentRecord record = AssessmentRecord.builder()
                .recordId(recordId).userId(1L).status("COMPLETED").build();
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        AssessmentRecord result = assessmentService.getRecord(recordId);

        assertNotNull(result);
        assertEquals(recordId, result.getRecordId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("getRecord — throws RuntimeException when not found")
    public void testGetRecord_NotFound() {
        when(recordRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assessmentService.getRecord(999L));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("getAllScales — returns empty list when none active")
    public void testGetAllScales_Empty() {
        when(scaleRepository.findByIsActiveTrueOrderByScaleIdAsc()).thenReturn(List.of());

        List<AssessmentScale> result = assessmentService.getAllScales();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
