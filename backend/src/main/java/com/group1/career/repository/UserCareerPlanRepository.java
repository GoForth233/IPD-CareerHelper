package com.group1.career.repository;

import com.group1.career.model.entity.UserCareerPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCareerPlanRepository extends JpaRepository<UserCareerPlan, Long> {
    Optional<UserCareerPlan> findByUserId(Long userId);
}
