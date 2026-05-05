package com.group1.career.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One "From the Field" card on the home feed — curated DB row, article
 * back-fill, or AI-personalised tip (see {@code HomeFieldTipsService}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeConsultationFeedDto {

    private Long id;
    private String title;
    private String body;
    private String author;
    /** In-app path ({@code /pages/...}) or https URL. */
    private String sourceUrl;
    private String imageUrl;
}
