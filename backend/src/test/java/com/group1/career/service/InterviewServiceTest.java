package com.group1.career.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    /** Used by {@code endInterview} to push the "interview complete" system
     *  notification onto the Messages tab. We don't assert on it here — the
     *  notification flow has its own test — so a no-op mock is enough. */
    @Mock
    private NotificationService notificationService;

    /** Used by endInterview/saveReport to merge the interview into the user's
     *  cross-tool portrait. The merge is best-effort and wrapped in try/catch,
     *  so a no-op mock is enough — none of these tests assert on the snapshot. */
    @Mock
    private UserProfileSnapshotService snapshotService;

    /** Used to parse the report JSON for strong/weak dimensions when one is
     *  supplied to saveReport. None of these tests pass a real report, so
     *  the mock is never actually invoked. */
    @Mock
    private ObjectMapper objectMapper;

    /** Sprint D-1 added a check-in trigger on endInterview; we never assert
     *  on it here so a no-op mock is enough. */
    @Mock
    private com.group1.career.service.CheckInService checkInService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    @Test
    @DisplayName("Test Start Interview")
    public void testStartInterview_Success() {
        Long userId = 1L;
        String positionName = "Java Engineer";

        Interview mockInterview = Interview.builder()
                .interviewId(1L).userId(userId).positionName(positionName).status("ONGOING").build();

        when(interviewRepository.save(any(Interview.class))).thenReturn(mockInterview);

        Interview result = interviewService.startInterview(userId, null, positionName, "Normal");

        assertNotNull(result);
        assertEquals("ONGOING", result.getStatus());
        assertEquals(positionName, result.getPositionName());
        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    @DisplayName("Test Send Message")
    public void testSendMessage_Success() {
        Long interviewId = 1L;
        String role = "USER";
        String content = "Hello, I'm ready for the interview";

        when(messageRepository.save(any(InterviewMessage.class)))
                .thenReturn(InterviewMessage.builder().msgId(1L).build());

        interviewService.sendMessage(interviewId, role, content);

        verify(messageRepository, times(1)).save(argThat(msg ->
            msg.getInterviewId().equals(interviewId) &&
            msg.getRole().equals(role) &&
            msg.getContent().equals(content)
        ));
    }

    @Test
    @DisplayName("Test Get Interview Messages")
    public void testGetInterviewMessages_Success() {
        Long interviewId = 1L;
        List<InterviewMessage> messages = Arrays.asList(
                InterviewMessage.builder().msgId(1L).content("Question 1").role("AI").build(),
                InterviewMessage.builder().msgId(2L).content("Answer 1").role("USER").build()
        );

        when(messageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId)).thenReturn(messages);

        List<InterviewMessage> result = interviewService.getInterviewMessages(interviewId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AI", result.get(0).getRole());
        verify(messageRepository, times(1)).findByInterviewIdOrderByCreatedAtAsc(interviewId);
    }

    @Test
    @DisplayName("Test End Interview")
    public void testEndInterview_Success() {
        Long interviewId = 1L;
        Integer finalScore = 85;

        Interview ongoingInterview = Interview.builder()
                .interviewId(interviewId).userId(1L).status("ONGOING")
                .startedAt(LocalDateTime.now().minusMinutes(30)).build();

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(ongoingInterview));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(i -> i.getArgument(0));

        Interview result = interviewService.endInterview(interviewId, finalScore);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(finalScore, result.getFinalScore());
        assertNotNull(result.getEndedAt());
        assertNotNull(result.getDurationSeconds());
        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    @DisplayName("Test Get User Interviews")
    public void testGetUserInterviews_Success() {
        Long userId = 1L;
        List<Interview> interviews = List.of(
                Interview.builder().interviewId(1L).status("COMPLETED").build()
        );

        when(interviewRepository.findByUserIdOrderByStartedAtDesc(userId)).thenReturn(interviews);

        List<Interview> result = interviewService.getUserInterviews(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(interviewRepository, times(1)).findByUserIdOrderByStartedAtDesc(userId);
    }

    @Test
    @DisplayName("Test Get Interview By ID")
    public void testGetInterviewById_Success() {
        Long interviewId = 1L;
        Interview mockInterview = Interview.builder().interviewId(interviewId).positionName("Frontend Dev").build();

        when(interviewRepository.findById(interviewId)).thenReturn(Optional.of(mockInterview));

        Interview result = interviewService.getInterviewById(interviewId);

        assertNotNull(result);
        assertEquals(interviewId, result.getInterviewId());
        assertEquals("Frontend Dev", result.getPositionName());
    }
}
