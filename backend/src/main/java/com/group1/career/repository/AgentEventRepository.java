package com.group1.career.repository;

import com.group1.career.model.entity.AgentEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentEventRepository extends JpaRepository<AgentEvent, Long> {

    List<AgentEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<AgentEvent> findTopByUserIdAndEventTypeOrderByCreatedAtDesc(Long userId, String eventType);
}
