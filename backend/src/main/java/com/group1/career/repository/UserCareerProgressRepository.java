package com.group1.career.repository;

import com.group1.career.model.entity.UserCareerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCareerProgressRepository extends JpaRepository<UserCareerProgress, Long> {
    List<UserCareerProgress> findByUserId(Long userId);
    Optional<UserCareerProgress> findByUserIdAndNodeId(Long userId, Long nodeId);
}

