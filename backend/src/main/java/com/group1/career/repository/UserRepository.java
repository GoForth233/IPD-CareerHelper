package com.group1.career.repository;

import com.group1.career.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByOrgId(Long orgId);

    /** F25: IDs of users whose 30-day grace period has elapsed. */
    @Query("SELECT u.userId FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt <= :cutoff")
    List<Long> findExpiredDeletionIds(LocalDateTime cutoff);
}

