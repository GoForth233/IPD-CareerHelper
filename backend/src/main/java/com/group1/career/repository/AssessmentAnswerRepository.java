package com.group1.career.repository;

import com.group1.career.model.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {
    List<AssessmentAnswer> findByRecordId(Long recordId);
}
