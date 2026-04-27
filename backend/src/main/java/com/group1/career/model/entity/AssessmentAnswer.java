package com.group1.career.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assessment_answers")
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;

    @Column(name = "score_snapshot", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal scoreSnapshot = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
