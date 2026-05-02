package com.group1.career.repository;

import com.group1.career.model.entity.AssistantSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantSessionRepository extends JpaRepository<AssistantSession, Long> {
    List<AssistantSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<AssistantSession> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
