package com.group1.career.config;

import com.group1.career.model.entity.Role;
import com.group1.career.model.entity.UserAuth;
import com.group1.career.model.entity.User;
import com.group1.career.model.entity.UserRole;
import com.group1.career.repository.RoleRepository;
import com.group1.career.repository.UserAuthRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class AuthBootstrapConfig {

    private static final String DEFAULT_ROLE_CODE = "STUDENT";
    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final String LEGACY_PASSWORD = "PASSWORD";
    private static final String EMAIL_PASSWORD = "EMAIL_PASSWORD";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner authBootstrapRunner() {
        return args -> {
            Role studentRole = roleRepository.findByRoleCode(DEFAULT_ROLE_CODE)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .roleCode(DEFAULT_ROLE_CODE)
                            .roleName("Student")
                            .description("Default role for student-side users")
                            .isSystem(true)
                            .build()));

            // ADMIN role for the B-side console and weekly-report manual trigger.
            // We seed the row only — granting the role to a specific user is
            // an explicit ops step, never automatic, to avoid accidentally
            // promoting a regular signup.
            roleRepository.findByRoleCode(ADMIN_ROLE_CODE)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .roleCode(ADMIN_ROLE_CODE)
                            .roleName("Administrator")
                            .description("Back-office staff with access to org dashboards and ops triggers")
                            .isSystem(true)
                            .build()));

            for (UserAuth auth : userAuthRepository.findAll()) {
                boolean changed = false;

                if (LEGACY_PASSWORD.equals(auth.getIdentityType()) && auth.getIdentifier() != null && auth.getIdentifier().contains("@")) {
                    auth.setIdentityType(EMAIL_PASSWORD);
                    auth.setIdentifier(auth.getIdentifier().trim().toLowerCase());
                    changed = true;
                }

                if (auth.getCredential() != null && !auth.getCredential().isBlank() && !isBcryptHash(auth.getCredential())) {
                    auth.setCredential(passwordEncoder.encode(auth.getCredential()));
                    changed = true;
                }

                if (changed) {
                    userAuthRepository.save(auth);
                }
            }

            for (User user : userRepository.findAll()) {
                if (!userRoleRepository.existsByUserIdAndRoleId(user.getUserId(), studentRole.getRoleId())) {
                    userRoleRepository.save(UserRole.builder()
                            .userId(user.getUserId())
                            .roleId(studentRole.getRoleId())
                            .build());
                }
            }
        };
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
