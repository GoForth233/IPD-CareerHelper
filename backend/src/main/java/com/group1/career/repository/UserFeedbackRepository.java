package com.group1.career.repository;

import com.group1.career.model.entity.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    List<UserFeedback> findByStatus(String status);
    Page<UserFeedback> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<UserFeedback> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
