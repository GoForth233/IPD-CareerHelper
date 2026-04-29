package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * In-app notification surfaced on the Messages > System tab.
 * Created by domain services when something noteworthy happens to the
 * current user (interview completed, assessment scored, etc.).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_user_created", columnList = "user_id, created_at")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Free-form short code, e.g. "INTERVIEW_COMPLETED", "ASSESSMENT_DONE". */
    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Optional deep link the client may use, e.g. "/pages/interview/report?interviewId=42". */
    @Column(name = "link", length = 255)
    private String link;

    /** false = unread, true = read. */
    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private Boolean readFlag = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
