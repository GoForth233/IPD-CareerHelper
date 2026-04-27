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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewMessageRepository messageRepository;

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
    }

    @Override
    public List<InterviewMessage> getInterviewMessages(Long interviewId) {
        return messageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId);
    }

    @Override
    @Transactional
    public Interview endInterview(Long interviewId, Integer finalScore) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));

        interview.setStatus("COMPLETED");
        interview.setFinalScore(finalScore);
        interview.setEndedAt(LocalDateTime.now());

        if (interview.getStartedAt() != null) {
            Duration duration = Duration.between(interview.getStartedAt(), interview.getEndedAt());
            interview.setDurationSeconds((int) duration.getSeconds());
        }

        return interviewRepository.save(interview);
    }

    @Override
    public List<Interview> getUserInterviews(Long userId) {
        return interviewRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    @Override
    public Interview getInterviewById(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));
    }
}
