package com.group1.career.repository;

import com.group1.career.model.entity.AssessmentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {
    List<AssessmentOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);
    List<AssessmentOption> findByQuestionIdIn(List<Long> questionIds);
}
