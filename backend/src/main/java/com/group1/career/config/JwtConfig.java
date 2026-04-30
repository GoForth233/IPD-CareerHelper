package com.group1.career.config;

import com.group1.career.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Pushes {@code jwt.secret} / {@code jwt.expiration-ms} from application.yml
 * into {@link JwtUtils} at Spring startup.
 *
 * <p>The secret must be at least 32 bytes — the application refuses to start
 * if {@code JWT_SECRET} is missing, so we never silently boot with a stale
 * dev key. Generate one with {@code openssl rand -hex 32}.</p>
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration-ms:604800000}")
    private long expirationMs;

    @PostConstruct
    public void init() {
        JwtUtils.configure(secret, expirationMs);
    }
}
