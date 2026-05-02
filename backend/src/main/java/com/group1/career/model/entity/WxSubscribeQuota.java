package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * F10: WeChat subscribe message quota per (user, template).
 *
 * <p>When the user clicks "订阅" in the mini-program the frontend calls
 * {@code wx.requestSubscribeMessage} and the result is reported to the
 * backend, which increments {@code remaining} here. Before sending a
 * subscribe push the notification job checks remaining > 0 and decrements
 * atomically (via Redis + DB update). Sending without quota is blocked by
 * WeChat's platform.</p>
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wx_subscribe_quota",
        uniqueConstraints = @UniqueConstraint(name = "uk_wx_quota_user_tpl", columnNames = {"user_id", "template_id"}))
public class WxSubscribeQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** WeChat template ID as returned by mp.weixin.qq.com. */
    @Column(name = "template_id", length = 64, nullable = false)
    private String templateId;

    @Column(name = "remaining", nullable = false)
    @Builder.Default
    private Integer remaining = 0;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
