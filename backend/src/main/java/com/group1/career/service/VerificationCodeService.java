package com.group1.career.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存式验证码服务。
 * 每个邮箱+用途组合独立管理，支持：过期清理、防刷（60秒冷却）、最多验证3次。
 */
@Slf4j
@Service
public class VerificationCodeService {

    private static final long EXPIRE_MS = 5 * 60 * 1000L;     // 5 分钟有效
    private static final long COOLDOWN_MS = 60 * 1000L;        // 60 秒发送冷却
    private static final int MAX_ATTEMPTS = 5;                  // 最多验证 5 次

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, CodeEntry> store = new ConcurrentHashMap<>();

    public String generateAndStore(String email, String purpose) {
        String key = buildKey(email, purpose);
        CodeEntry existing = store.get(key);

        if (existing != null && !existing.isExpired()) {
            long elapsed = Instant.now().toEpochMilli() - existing.createdAt;
            if (elapsed < COOLDOWN_MS) {
                long waitSec = (COOLDOWN_MS - elapsed) / 1000;
                throw new RuntimeException("请等待 " + waitSec + " 秒后再发送");
            }
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(key, new CodeEntry(code, Instant.now().toEpochMilli()));
        log.info("Generated verification code for {} [{}]", email, purpose);
        return code;
    }

    /**
     * 验证验证码，验证成功后立即作废。
     */
    public boolean verify(String email, String purpose, String inputCode) {
        String key = buildKey(email, purpose);
        CodeEntry entry = store.get(key);

        if (entry == null || entry.isExpired()) {
            return false;
        }
        if (entry.attempts >= MAX_ATTEMPTS) {
            store.remove(key);
            return false;
        }

        entry.attempts++;
        if (entry.code.equals(inputCode)) {
            store.remove(key);
            return true;
        }
        return false;
    }

    private String buildKey(String email, String purpose) {
        return email.toLowerCase().trim() + ":" + purpose;
    }

    /** 每 10 分钟清理过期条目，防止内存泄漏 */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void evictExpired() {
        int removed = 0;
        for (var it = store.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue().isExpired()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("Evicted {} expired verification codes", removed);
        }
    }

    private static class CodeEntry {
        final String code;
        final long createdAt;
        int attempts = 0;

        CodeEntry(String code, long createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }

        boolean isExpired() {
            return Instant.now().toEpochMilli() - createdAt > EXPIRE_MS;
        }
    }
}
