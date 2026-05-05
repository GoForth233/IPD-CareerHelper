package com.group1.career.repository;

import com.group1.career.model.entity.AgentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentStateRepository extends JpaRepository<AgentState, Long> {
    Optional<AgentState> findByUserId(Long userId);
}
