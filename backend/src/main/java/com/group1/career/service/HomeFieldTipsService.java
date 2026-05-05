package com.group1.career.service;

import com.group1.career.model.dto.HomeConsultationFeedDto;
import com.group1.career.model.entity.HomeArticle;
import com.group1.career.model.entity.HomeConsultation;

import java.util.List;

/**
 * Builds the home-page "From the Field" consultation cards: personalised AI
 * tips for logged-in users (cached per day), with DB + article fallback.
 */
public interface HomeFieldTipsService {

    List<HomeConsultationFeedDto> buildConsultationFeed(
            Long userId,
            long seed,
            int limit,
            List<HomeConsultation> consultationPool,
            List<HomeArticle> articlePool
    );
}
