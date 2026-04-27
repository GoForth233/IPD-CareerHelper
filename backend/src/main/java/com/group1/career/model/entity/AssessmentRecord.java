package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assessment_records")
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "result_summary", length = 100)
    private String resultSummary;

    /** JSON 字符串，存储计算出的各维度得分，如 {"E":5,"I":3,"S":4,"N":6,...} */
    @Column(name = "result_json", columnDefinition = "json")
    private String resultJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
