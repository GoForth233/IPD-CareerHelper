package com.group1.career.job;

import com.group1.career.repository.AssistantMessageRepository;
import com.group1.career.repository.AssistantSessionRepository;
import com.group1.career.repository.ConversationSummaryRepository;
import com.group1.career.repository.UserAuthRepository;
import com.group1.career.repository.UserFactRepository;
import com.group1.career.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * F25: Nightly hard-deletion of accounts whose 30-day grace period has expired.
 *
 * <p>Runs every night at 03:00 server time. Finds users with
 * {@code deleted_at <= now() - 30 days} and deletes all their personal data
 * (PII) while keeping the {@code account_deletion_log} row for compliance.</p>
 *
 * <p>Deletion order respects FK constraints (messages → sessions → auth → user).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDeletionCleanupJob {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final AssistantMessageRepository assistantMessageRepository;
    private final AssistantSessionRepository assistantSessionRepository;
    private final ConversationSummaryRepository conversationSummaryRepository;
    private final UserFactRepository userFactRepository;

    private static final int GRACE_DAYS = 30;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void run() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GRACE_DAYS);
        List<Long> expiredIds = userRepository.findExpiredDeletionIds(cutoff);
        if (expiredIds.isEmpty()) return;

        log.info("[F25] Hard-deleting {} expired accounts: {}", expiredIds.size(), expiredIds);

        for (Long userId : expiredIds) {
            try {
                hardDelete(userId);
            } catch (Exception e) {
                log.error("[F25] Failed to hard-delete user {}: {}", userId, e.getMessage(), e);
            }
        }
    }

    private void hardDelete(Long userId) {
        assistantSessionRepository.findByUserId(userId).forEach(session -> {
            assistantMessageRepository.deleteBySessionId(session.getSessionId());
        });
        assistantSessionRepository.deleteByUserId(userId);
        conversationSummaryRepository.deleteByUserId(userId);
        userFactRepository.deleteByUserId(userId);
        userAuthRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("[F25] Hard-deleted user {}", userId);
    }
}
