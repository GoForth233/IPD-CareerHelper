package com.group1.career.service;

import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Notification;
import com.group1.career.repository.NotificationRepository;
import com.group1.career.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("push — saves and returns notification")
    public void testPush_Success() {
        Notification saved = Notification.builder()
                .notificationId(1L).userId(1L).type("INTERVIEW_COMPLETED")
                .title("Interview done").readFlag(false).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        Notification result = notificationService.push(
                1L, "INTERVIEW_COMPLETED", "Interview done", "body text", "/pages/interview");

        assertNotNull(result);
        assertEquals(1L, result.getNotificationId());
        assertEquals("INTERVIEW_COMPLETED", result.getType());
        assertFalse(result.getReadFlag());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("push — returns null for null userId (never throws)")
    public void testPush_NullUserId() {
        Notification result = notificationService.push(null, "TYPE", "title", "body", "/link");

        assertNull(result);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    @DisplayName("listForUser — returns ordered notification list")
    public void testListForUser() {
        Long userId = 5L;
        List<Notification> notifications = List.of(
                Notification.builder().notificationId(2L).userId(userId).title("Newer").build(),
                Notification.builder().notificationId(1L).userId(userId).title("Older").build()
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(notifications);

        List<Notification> result = notificationService.listForUser(userId);

        assertEquals(2, result.size());
        assertEquals("Newer", result.get(0).getTitle());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("unreadCount — returns count from repository")
    public void testUnreadCount() {
        when(notificationRepository.countByUserIdAndReadFlagFalse(3L)).thenReturn(7L);

        long count = notificationService.unreadCount(3L);

        assertEquals(7L, count);
        verify(notificationRepository).countByUserIdAndReadFlagFalse(3L);
    }

    @Test
    @DisplayName("markRead — flips readFlag to true and saves")
    public void testMarkRead_Success() {
        Long userId = 1L;
        Notification n = Notification.builder()
                .notificationId(10L).userId(userId).readFlag(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenReturn(n);

        notificationService.markRead(10L, userId);

        assertTrue(n.getReadFlag());
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("markRead — idempotent: skip save when already read")
    public void testMarkRead_AlreadyRead_Idempotent() {
        Long userId = 1L;
        Notification n = Notification.builder()
                .notificationId(10L).userId(userId).readFlag(true).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        notificationService.markRead(10L, userId);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markRead — throws BizException when notification not found")
    public void testMarkRead_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> notificationService.markRead(99L, 1L));
    }

    @Test
    @DisplayName("markRead — throws BizException when userId does not match")
    public void testMarkRead_WrongOwner() {
        Notification n = Notification.builder()
                .notificationId(10L).userId(99L).readFlag(false).build();
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(n));

        assertThrows(BizException.class, () -> notificationService.markRead(10L, 1L));
    }

    @Test
    @DisplayName("markAllRead — marks only unread, returns correct count")
    public void testMarkAllRead() {
        Long userId = 1L;
        List<Notification> all = List.of(
                Notification.builder().notificationId(1L).userId(userId).readFlag(false).build(),
                Notification.builder().notificationId(2L).userId(userId).readFlag(false).build(),
                Notification.builder().notificationId(3L).userId(userId).readFlag(true).build()
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(all);

        int updated = notificationService.markAllRead(userId);

        assertEquals(2, updated);
        verify(notificationRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 2));
    }

    @Test
    @DisplayName("markAllRead — returns 0 when all already read")
    public void testMarkAllRead_AllAlreadyRead() {
        Long userId = 1L;
        List<Notification> all = List.of(
                Notification.builder().notificationId(1L).userId(userId).readFlag(true).build()
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(all);

        int updated = notificationService.markAllRead(userId);

        assertEquals(0, updated);
    }
}
