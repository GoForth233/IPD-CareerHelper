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
 * Career "consultation" — short tip / industry insight cards shown at the
 * bottom of the home feed. Distinct from {@link HomeArticle} so the home
 * layout can render them with a different affordance (compact list vs full
 * cover-card) and so different editorial calendars don't collide.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "home_consultations", indexes = {
        @Index(name = "idx_home_consult_published", columnList = "published_at")
})
public class HomeConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "body_md", columnDefinition = "TEXT")
    private String bodyMd;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(name = "published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
