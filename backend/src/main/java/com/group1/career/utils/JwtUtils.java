package com.group1.career.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Slf4j
public class JwtUtils {

    // Default fallback (overridable via JwtConfig at startup using jwt.secret in application.yml)
    private static final String DEFAULT_SECRET = "CareerPlatformSuperSecretKeyThatIsAtLeast32BytesLong!!!";
    private static volatile Key KEY = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes());

    // Token validity (default 24 hours, override via JwtConfig)
    private static volatile long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /** Initialized by JwtConfig at Spring startup. Safe to call once at boot. */
    public static void configure(String secret, long expirationMs) {
        if (secret != null && secret.getBytes().length >= 32) {
            KEY = Keys.hmacShaKeyFor(secret.getBytes());
        } else {
            log.warn("JWT secret too short (<32 bytes); falling back to default key");
        }
        if (expirationMs > 0) {
            EXPIRATION_TIME = expirationMs;
        }
    }

    /**
     * Generate JWT
     */
    public static String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse Token and return User ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Validate Token
     */
    public static boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
        } catch (JwtException e) {
            log.error("Invalid JWT signature");
        }
        return false;
    }
}
