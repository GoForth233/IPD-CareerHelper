package com.group1.career.config;

import com.group1.career.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Pushes jwt.secret / jwt.expiration-ms from application.yml into the static JwtUtils
 * at Spring startup. Keeps JwtUtils' static API stable while letting ops rotate
 * the secret via env var (JWT_SECRET) without code changes.
 */
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    @PostConstruct
    public void init() {
        JwtUtils.configure(secret, expirationMs);
    }
}
