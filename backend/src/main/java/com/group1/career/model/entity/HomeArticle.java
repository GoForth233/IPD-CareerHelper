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
 * Career article surfaced on the home page.
 * Today these are seeded by {@code DataLoaderConfig}; future iteration can
 * pull from RSS / partner feeds. Tapping a card opens {@code source_url}
 * via the shared {@code openLink} util (web-view + clipboard fallback).
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "home_articles", indexes = {
        @Index(name = "idx_home_articles_published", columnList = "published_at")
})
public class HomeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "summary", length = 500)
    private String summary;

    /** Optional cover image — falls back to a tinted gradient on the client. */
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    /** Link the user opens. Internal /pages/... allowed; otherwise external https. */
    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    /** Loose category for filtering, e.g. "interview", "resume", "skill". */
    @Column(name = "category", length = 32)
    private String category;

    /** Origin: MANUAL (admin-entered) | RSS_JUEJIN | RSS_36KR */
    @Column(name = "source", length = 20)
    @Builder.Default
    private String source = "MANUAL";

    /** Admin-pinned articles appear at the top of the home feed. */
    @Column(name = "pinned", nullable = false)
    @Builder.Default
    private Boolean pinned = false;

    /** Hidden articles are excluded from the home feed. */
    @Column(name = "hidden", nullable = false)
    @Builder.Default
    private Boolean hidden = false;

    @Column(name = "published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
