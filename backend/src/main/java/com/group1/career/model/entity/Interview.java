package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "position_name", length = 50)
    private String positionName;

    @Column(name = "difficulty", length = 20)
    @Builder.Default
    private String difficulty = "Normal"; // Easy, Normal, Hard

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ONGOING"; // ONGOING, COMPLETED, CANCELLED

    @Column(name = "final_score")
    private Integer finalScore;

    @Column(name = "report_mongo_id", length = 50)
    private String reportMongoId;

    /** Cached AI-generated report (JSON serialized InterviewReportDto). */
    @Column(name = "report_json", columnDefinition = "LONGTEXT")
    private String reportJson;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}

