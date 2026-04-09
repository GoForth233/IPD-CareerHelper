package com.group1.career.service;

import com.group1.career.model.document.AssessmentResultDocument;

import java.util.List;
import java.util.Map;

public interface AssessmentService {

    /**
     * Submit raw answers from a quiz and compute scoring traits
     */
    AssessmentResultDocument submitAndScore(Long userId, String assessmentType, Map<String, String> rawAnswers);

    /**
     * Retrieve all assessment results for a user
     */
    List<AssessmentResultDocument> getUserResults(Long userId);
}
