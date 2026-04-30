package com.group1.career.service;

import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Role;
import com.group1.career.model.entity.UserRole;
import com.group1.career.repository.RoleRepository;
import com.group1.career.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Lightweight role check used by every admin endpoint. We deliberately keep
 * this off the Spring Security {@code @PreAuthorize} stack — the rest of the
 * app uses a custom JwtUtils + AuthInterceptor pipeline, and adding a second
 * security stack just to gate three endpoints isn't worth the complexity.
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    public static final String ADMIN_ROLE_CODE = "ADMIN";

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public boolean isAdmin(Long userId) {
        if (userId == null || userId <= 0) return false;
        Optional<Role> admin = roleRepository.findByRoleCode(ADMIN_ROLE_CODE);
        if (admin.isEmpty()) return false;
        Long adminRoleId = admin.get().getRoleId();
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .anyMatch(adminRoleId::equals);
    }

    /** Throw FORBIDDEN if the caller is not an admin. */
    public void requireAdmin(Long userId) {
        if (!isAdmin(userId)) throw new BizException(ErrorCode.FORBIDDEN);
    }
}
