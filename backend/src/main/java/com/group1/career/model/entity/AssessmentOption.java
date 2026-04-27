package com.group1.career.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assessment_options")
public class AssessmentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "option_label", length = 20)
    private String optionLabel;

    @Column(name = "option_text", nullable = false, length = 255)
    private String optionText;

    @Column(name = "score_value", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal scoreValue = BigDecimal.ZERO;

    @Column(name = "dimension_code", length = 50)
    private String dimensionCode;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
