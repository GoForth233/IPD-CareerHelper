package com.group1.career.repository;

import com.group1.career.model.entity.AssessmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {
    List<AssessmentRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AssessmentRecord> findByUserIdAndScaleId(Long userId, Long scaleId);
}
