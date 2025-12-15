package com.group1.career.service.impl;

import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;
import com.group1.career.repository.InterviewMessageRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String INTERVIEW_HISTORY_PREFIX = "interview:history:";
    private static final String INTERVIEW_MESSAGES_PREFIX = "interview:messages:";

    @Override
    @Transactional
    public Interview startInterview(Long userId, Long resumeId, String positionName, String difficulty) {
        Interview interview = Interview.builder()
                .userId(userId)
                .resumeId(resumeId)
                .positionName(positionName)
                .difficulty(difficulty)
                .status("ONGOING")
                .build();

        Interview saved = interviewRepository.save(interview);
        log.info("Started interview {} for user {}", saved.getInterviewId(), userId);

        // Clear user interview history cache
        redisTemplate.delete(INTERVIEW_HISTORY_PREFIX + userId);

        return saved;
    }

    @Override
    @Transactional
    public void sendMessage(Long interviewId, String role, String content) {
        InterviewMessage message = InterviewMessage.builder()
                .interviewId(interviewId)
                .role(role)
                .content(content)
                .build();

        messageRepository.save(message);

        // Clear interview messages cache
        redisTemplate.delete(INTERVIEW_MESSAGES_PREFIX + interviewId);
    }

    @Override
    public List<InterviewMessage> getInterviewMessages(Long interviewId) {
        String cacheKey = INTERVIEW_MESSAGES_PREFIX + interviewId;

        // 1. Check Cache
        @SuppressWarnings("unchecked")
        List<InterviewMessage> cachedMessages = (List<InterviewMessage>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMessages != null) {
            log.info("Redis Cache Hit: {}", cacheKey);
            return cachedMessages;
        }

        // 2. Query DB
        List<InterviewMessage> messages = messageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId);

        // 3. Write Cache (10 minutes)
        redisTemplate.opsForValue().set(cacheKey, messages, 10, TimeUnit.MINUTES);

        return messages;
    }

    @Override
    @Transactional
    public Interview endInterview(Long interviewId, Integer finalScore) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));

        interview.setStatus("COMPLETED");
        interview.setFinalScore(finalScore);
        interview.setEndedAt(LocalDateTime.now());

        // Calculate duration
        if (interview.getStartedAt() != null) {
            Duration duration = Duration.between(interview.getStartedAt(), interview.getEndedAt());
            interview.setDurationSeconds((int) duration.getSeconds());
        }

        Interview updated = interviewRepository.save(interview);

        // Clear caches
        redisTemplate.delete(INTERVIEW_HISTORY_PREFIX + interview.getUserId());
        redisTemplate.delete(INTERVIEW_MESSAGES_PREFIX + interviewId);

        return updated;
    }

    @Override
    public List<Interview> getUserInterviews(Long userId) {
        String cacheKey = INTERVIEW_HISTORY_PREFIX + userId;

        // 1. Check Cache
        @SuppressWarnings("unchecked")
        List<Interview> cachedInterviews = (List<Interview>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedInterviews != null) {
            log.info("Redis Cache Hit: {}", cacheKey);
            return cachedInterviews;
        }

        // 2. Query DB
        List<Interview> interviews = interviewRepository.findByUserIdOrderByStartedAtDesc(userId);

        // 3. Write Cache (30 minutes)
        redisTemplate.opsForValue().set(cacheKey, interviews, 30, TimeUnit.MINUTES);

        return interviews;
    }

    @Override
    public Interview getInterviewById(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));
    }
}

