package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "target_job", length = 50)
    private String targetJob;

    /**
     * OSS object key, e.g. {@code resumes/uuid.pdf}.
     * Historically this column held a full URL; we now store only the key
     * so the bucket / endpoint can change without touching every row.
     * Frontend should never load this value directly — use {@link #fileViewUrl}.
     */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /**
     * Short-lived signed URL the frontend can fetch directly. Populated by
     * {@code ResumeService.hydrateUrl} just before serialization; never persisted.
     */
    @Transient
    private String fileViewUrl;

    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "v1.0";

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "UPLOADED"; // UPLOADED, PARSING, COMPLETED

    /** 简历解析后的结构化内容（JSON 字符串），包含 education/projects/skills/rawContent */
    @Column(name = "parsed_content", columnDefinition = "json")
    private String parsedContent;

    @Column(name = "diagnosis_score")
    @Builder.Default
    private Integer diagnosisScore = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

