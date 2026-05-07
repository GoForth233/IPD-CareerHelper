package com.group1.career.service.impl;

import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Notification;
import com.group1.career.repository.NotificationRepository;
import com.group1.career.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification push(Long userId, String type, String title, String content, String link) {
        if (userId == null) return null;
        try {
            Notification n = Notification.builder()
                    .userId(userId)
                    .type(type)
                    .title(title)
                    .content(content)
                    .link(link)
                    .readFlag(false)
                    .build();
            return notificationRepository.save(n);
        } catch (Exception e) {
            // Notifications are non-critical -- never let a write failure
            // bubble out and break the calling business flow (interview end,
            // assessment submission, etc.).
            log.warn("[notification] push failed for user {} type {}: {}", userId, type, e.toString());
            return null;
        }
    }

    @Override
    public List<Notification> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BizException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new BizException("You don't own this notification");
        }
        if (Boolean.TRUE.equals(n.getReadFlag())) return;
        n.setReadFlag(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !Boolean.TRUE.equals(n.getReadFlag()))
                .toList();
        unread.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Override
    @Transactional
    public void delete(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BizException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new BizException("You don't own this notification");
        }
        notificationRepository.delete(n);
    }
}
