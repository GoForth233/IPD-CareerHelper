package com.group1.career.repository;

import com.group1.career.model.entity.UserFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFactRepository extends JpaRepository<UserFact, Long> {
    List<UserFact> findByUserId(Long userId);
    List<UserFact> findByUserIdAndCategory(Long userId, String category);
    Optional<UserFact> findByUserIdAndFactKey(Long userId, String factKey);
    void deleteByUserId(Long userId);
}
