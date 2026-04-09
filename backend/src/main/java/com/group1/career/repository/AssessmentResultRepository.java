package com.group1.career.repository;

import com.group1.career.model.document.AssessmentResultDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentResultRepository extends MongoRepository<AssessmentResultDocument, String> {
    List<AssessmentResultDocument> findByUserId(Long userId);
    List<AssessmentResultDocument> findByUserIdAndAssessmentType(Long userId, String type);
}
