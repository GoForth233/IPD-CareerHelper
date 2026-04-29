package com.group1.career.service;

import com.group1.career.model.entity.Notification;

import java.util.List;

public interface NotificationService {

    /** Push a new notification for a user. Best-effort -- callers should never let a failure here break their main flow. */
    Notification push(Long userId, String type, String title, String content, String link);

    List<Notification> listForUser(Long userId);

    long unreadCount(Long userId);

    /** Mark a single notification read; verifies ownership. */
    void markRead(Long notificationId, Long userId);

    /** Mark every unread notification belonging to user as read. */
    int markAllRead(Long userId);
}
