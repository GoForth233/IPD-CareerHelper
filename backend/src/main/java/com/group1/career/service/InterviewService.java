package com.group1.career.service;

import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewMessage;

import java.util.List;

public interface InterviewService {
    /**
     * Start a new interview
     */
    Interview startInterview(Long userId, Long resumeId, String positionName, String difficulty);

    /**
     * Send a message in interview
     */
    void sendMessage(Long interviewId, String role, String content);

    /**
     * Get interview history messages
     */
    List<InterviewMessage> getInterviewMessages(Long interviewId);

    /**
     * End interview and calculate final score
     */
    Interview endInterview(Long interviewId, Integer finalScore);

    /**
     * Get user's all interviews
     */
    List<Interview> getUserInterviews(Long userId);

    /**
     * Get interview by ID
     */
    Interview getInterviewById(Long interviewId);
}

