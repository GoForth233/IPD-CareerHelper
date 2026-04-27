package com.group1.career.repository;

import com.group1.career.model.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRole> findByUserId(Long userId);
}
