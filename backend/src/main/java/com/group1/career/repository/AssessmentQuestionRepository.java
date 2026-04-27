package com.group1.career.repository;

import com.group1.career.model.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    List<AssessmentQuestion> findByScaleIdAndIsActiveTrueOrderBySortOrderAsc(Long scaleId);
}
