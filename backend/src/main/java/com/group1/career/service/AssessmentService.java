package com.group1.career.service;

import com.group1.career.model.entity.AssessmentQuestion;
import com.group1.career.model.entity.AssessmentRecord;
import com.group1.career.model.entity.AssessmentScale;

import java.util.List;
import java.util.Map;

public interface AssessmentService {

    List<AssessmentScale> getAllScales();

    List<AssessmentQuestion> getScaleQuestions(Long scaleId);

    /**
     * Submit answers and compute result, save to MySQL.
     * answers: questionId -> optionId (for SINGLE/MULTIPLE questions)
     */
    AssessmentRecord submitAndScore(Long userId, Long scaleId, Map<Long, Long> answers);

    List<AssessmentRecord> getUserRecords(Long userId);

    AssessmentRecord getRecord(Long recordId);
}
