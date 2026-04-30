package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Career-related Bilibili video harvested by the daily refresh job.
 * Frontend renders these as the home page's "Career Videos" section; tapping
 * a card opens the bilibili.com URL inside a {@code <web-view>} (the WeChat
 * MP business-domain whitelist must include {@code *.bilibili.com}).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "home_videos", indexes = {
        @Index(name = "uniq_home_videos_bvid", columnList = "bvid", unique = true),
        @Index(name = "idx_home_videos_keyword", columnList = "keyword"),
        @Index(name = "idx_home_videos_fetched_at", columnList = "fetched_at")
})
public class HomeVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Bilibili video id (e.g. BV1xx411c7mu). Unique. */
    @Column(name = "bvid", length = 32, nullable = false)
    private String bvid;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    /** HTTPS cover image URL. Bilibili returns http; we rewrite to https. */
    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(name = "up_name", length = 100)
    private String upName;

    /** UP主 mid — used by future "follow this UP" feature. */
    @Column(name = "up_mid")
    private Long upMid;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "view_count")
    private Long viewCount;

    /** The keyword that surfaced this video (面试 / 简历 / Java …). */
    @Column(name = "keyword", length = 64)
    private String keyword;

    /**
     * Sort score used to rank the daily batch. Higher = more prominent.
     * We seed it with view_count and decay daily so fresh batches surface.
     */
    @Column(name = "sort_score")
    @Builder.Default
    private Long sortScore = 0L;

    @CreationTimestamp
    @Column(name = "fetched_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fetchedAt;
}
