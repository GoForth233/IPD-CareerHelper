package com.group1.career.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Slf4j
public class JwtUtils {

    // Must be at least 256 bits (32 bytes) long
    private static final String SECRET_KEY_STRING = "CareerPlatformSuperSecretKeyThatIsAtLeast32BytesLong!!!";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // Token validity (e.g., 24 hours)
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

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
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}
