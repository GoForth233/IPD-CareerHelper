package com.group1.career.repository;

import com.group1.career.model.entity.AgentUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentUserProfileRepository extends JpaRepository<AgentUserProfile, Long> {
    Optional<AgentUserProfile> findByUserId(Long userId);
}
