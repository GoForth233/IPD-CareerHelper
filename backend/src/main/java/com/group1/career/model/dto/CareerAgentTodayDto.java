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
public class CareerAgentTodayDto {
    private String stage;
    private String riskLevel;
    private String headline;
    private String reason;
    private String todayFocus;
    private Integer progressPercent;
    private List<String> riskReasons;
    private List<Action> actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        private String label;
        private String target;
        private String type;
        private String priority;
        private String source;
    }
}
