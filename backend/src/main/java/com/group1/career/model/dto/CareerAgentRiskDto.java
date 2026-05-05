package com.group1.career.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerAgentRiskDto {
    private String overallLevel;
    private String primaryRiskCode;
    private String primaryRiskTitle;
    private String summary;
    private List<RiskItem> risks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {
        private String code;
        private String title;
        private String titleKey;
        private String level;
        private String trend;
        private Integer score;
        private String reason;
        private String reasonKey;
        private String recommendation;
        private String recommendationKey;
    }
}
