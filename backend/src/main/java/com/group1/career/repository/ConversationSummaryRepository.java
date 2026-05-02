package com.group1.career.repository;

import com.group1.career.model.entity.ConversationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {
    Optional<ConversationSummary> findByUserIdAndPersona(Long userId, String persona);
    void deleteByUserId(Long userId);
}
