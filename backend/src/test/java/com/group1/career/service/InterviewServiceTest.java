package com.group1.career.service;

import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.repository.InterviewMessageRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.service.impl.InterviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewMessageRepository messageRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    @Test
    @DisplayName("Test Start Interview")
    public void testStartInterview_Success() {
        // Prepare
        Long userId = 1L;
        Long resumeId = 100L;
        String positionName = "Java Engineer";
        String difficulty = "Normal";

        Interview mockInterview = Interview.builder()
                .interviewId(1L)
                .userId(userId)
                .positionName(positionName)
                .status("ONGOING")
                .build();

        when(interviewRepository.save(any(Interview.class))).thenReturn(mockInterview);

        // Execute
        Interview result = interviewService.startInterview(userId, resumeId, positionName, difficulty);

        // Verify
        assertNotNull(result);
        assertEquals("ONGOING", result.getStatus());
        assertEquals(positionName, result.getPositionName());
        verify(interviewRepository, times(1)).save(any(Interview.class));
        verify(redisTemplate, times(1)).delete("interview:history:" + userId);
    }

    @Test
    @DisplayName("Test Send Message")
    public void testSendMessage_Success() {
        // Prepare
        Long interviewId = 1L;
        String role = "USER";
        String content = "Hello, I'm ready for the interview";

        when(messageRepository.save(any(InterviewMessage.class)))
                .thenReturn(InterviewMessage.builder().msgId(1L).build());

        // Execute
        interviewService.sendMessage(interviewId, role, content);

        // Verify
        verify(messageRepository, times(1)).save(argThat(msg ->
            msg.getInterviewId().equals(interviewId) &&
            msg.getRole().equals(role) &&
            msg.getContent().equals(content)
        ));
        verify(redisTemplate, times(1)).delete("interview:messages:" + interviewId);
    }

    @Test
    @DisplayName("Test Get Interview Messages - Cache Miss")
    public void testGetInterviewMessages_CacheMiss() {
        // Prepare
        Long interviewId = 1L;
        String cacheKey = "interview:messages:" + interviewId;
        
        List<InterviewMessage> messages = Arrays.asList(
                InterviewMessage.builder().msgId(1L).content("Question 1").role("AI").build(),
                InterviewMessage.builder().msgId(2L).content("Answer 1").role("USER").build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(messageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId))
                .thenReturn(messages);

        // Execute
        List<InterviewMessage> result = interviewService.getInterviewMessages(interviewId);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AI", result.get(0).getRole());
        verify(messageRepository, times(1)).findByInterviewIdOrderByCreatedAtAsc(interviewId);
        verify(valueOperations, times(1)).set(eq(cacheKey), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Test End Interview")
    public void testEndInterview_Success() {
        // Prepare
        Long interviewId = 1L;
        Integer finalScore = 85;
        
        Interview ongoingInterview = Interview.builder()
                .interviewId(interviewId)
                .userId(1L)
                .status("ONGOING")
                .startedAt(LocalDateTime.now().minusMinutes(30))
                .build();

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(ongoingInterview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        Interview result = interviewService.endInterview(interviewId, finalScore);

        // Verify
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(finalScore, result.getFinalScore());
        assertNotNull(result.getEndedAt());
        assertNotNull(result.getDurationSeconds());
        
        verify(interviewRepository, times(1)).save(any(Interview.class));
        verify(redisTemplate, times(1)).delete("interview:history:" + ongoingInterview.getUserId());
        verify(redisTemplate, times(1)).delete("interview:messages:" + interviewId);
    }

    @Test
    @DisplayName("Test Get User Interviews - Cache Hit")
    public void testGetUserInterviews_CacheHit() {
        // Prepare
        Long userId = 1L;
        String cacheKey = "interview:history:" + userId;
        
        List<Interview> cachedInterviews = Arrays.asList(
                Interview.builder().interviewId(1L).status("COMPLETED").build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(cachedInterviews);

        // Execute
        List<Interview> result = interviewService.getUserInterviews(userId);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(interviewRepository, never()).findByUserIdOrderByStartedAtDesc(userId);
    }

    @Test
    @DisplayName("Test Get Interview By ID")
    public void testGetInterviewById_Success() {
        // Prepare
        Long interviewId = 1L;
        Interview mockInterview = Interview.builder()
                .interviewId(interviewId)
                .positionName("Frontend Dev")
                .build();

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(mockInterview));

        // Execute
        Interview result = interviewService.getInterviewById(interviewId);

        // Verify
        assertNotNull(result);
        assertEquals(interviewId, result.getInterviewId());
        assertEquals("Frontend Dev", result.getPositionName());
    }
}

