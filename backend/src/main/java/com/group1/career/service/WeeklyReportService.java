package com.group1.career.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.Interview;
import com.group1.career.repository.InterviewRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Weekly progress report — one notification per active user per Monday morning.
 *
 * <p>"Active" = has at least one COMPLETED interview in the last 14 days, so
 * we have something to compare against. We average the {@code radarChart}
 * fields per week, hand the deltas to Qwen, and persist the resulting
 * 50-word note as a notification with type {@code WEEKLY_REPORT}. The user
 * sees it on their Messages > System tab without any extra UI work.</p>
 *
 * <p>The job is intentionally fault-tolerant: a single user's failure (bad
 * report JSON, AI call timeout, DB hiccup) is logged but never aborts the
 * whole batch.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    /** Dimensions we surface in the recap. Mirrors the radarChart contract from InterviewReportController. */
    private static final List<String> DIMENSIONS = List.of(
            "technical", "communication", "logic", "expression", "pressureResistance"
    );
    /** Two weeks (this week + previous week) covers the comparison window. */
    private static final int LOOKBACK_DAYS = 14;

    private final InterviewRepository interviewRepository;
    private final NotificationService notificationService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final WechatSubscribeService wechatSubscribeService;

    /**
     * Build and push the report for every eligible user. Returns a small
     * summary so the admin trigger can show "n delivered, m skipped".
     */
    @Transactional
    public RunSummary runForAll() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusDays(LOOKBACK_DAYS);
        // Cut the window at the moment "this week" begins, i.e. 7 days ago.
        LocalDateTime weekBoundary = now.minusDays(7);

        List<Long> userIds = interviewRepository.findActiveUserIdsBetween(windowStart, now);
        log.info("[weekly-report] found {} active users in last {} days", userIds.size(), LOOKBACK_DAYS);

        int delivered = 0;
        int skipped = 0;
        for (Long uid : userIds) {
            try {
                if (runForUser(uid, windowStart, weekBoundary, now)) {
                    delivered++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
                log.warn("[weekly-report] user {} failed: {}", uid, e.toString());
            }
        }

        return RunSummary.builder()
                .totalCandidates(userIds.size())
                .delivered(delivered)
                .skipped(skipped)
                .runAt(now)
                .build();
    }

    /**
     * Build and deliver one user's report. Returns false when there isn't
     * enough comparison data (e.g. the user only has interviews in this
     * week — nothing to compare against).
     */
    @Transactional
    public boolean runForUser(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return runForUser(userId, now.minusDays(LOOKBACK_DAYS), now.minusDays(7), now);
    }

    private boolean runForUser(Long userId, LocalDateTime windowStart,
                                LocalDateTime weekBoundary, LocalDateTime now) {
        List<Interview> all = interviewRepository.findByUserIdAndStatusAndStartedAtBetween(
                userId, "COMPLETED", windowStart, now);
        if (all.isEmpty()) return false;

        Map<String, double[]> totals = new HashMap<>();
        // Index 0 = "previous week" (before weekBoundary), index 1 = "this week".
        for (String d : DIMENSIONS) totals.put(d, new double[]{0, 0, 0, 0}); // sumPrev, countPrev, sumThis, countThis

        for (Interview iv : all) {
            JsonNode radar = parseRadar(iv.getReportJson());
            if (radar == null || !radar.isObject()) continue;
            int bucket = iv.getStartedAt() != null && iv.getStartedAt().isBefore(weekBoundary) ? 0 : 1;
            for (String d : DIMENSIONS) {
                JsonNode v = radar.get(d);
                if (v == null || !v.isNumber()) continue;
                double[] cell = totals.get(d);
                cell[bucket * 2] += v.asDouble();
                cell[bucket * 2 + 1] += 1;
            }
        }

        Map<String, DimensionDelta> deltas = new HashMap<>();
        boolean haveComparison = false;
        for (String d : DIMENSIONS) {
            double[] c = totals.get(d);
            double prevAvg = c[1] > 0 ? c[0] / c[1] : Double.NaN;
            double thisAvg = c[3] > 0 ? c[2] / c[3] : Double.NaN;
            if (!Double.isNaN(prevAvg) && !Double.isNaN(thisAvg)) haveComparison = true;
            deltas.put(d, DimensionDelta.builder()
                    .name(d)
                    .previousAvg(Double.isNaN(prevAvg) ? null : roundOne(prevAvg))
                    .thisAvg(Double.isNaN(thisAvg) ? null : roundOne(thisAvg))
                    .delta(Double.isNaN(prevAvg) || Double.isNaN(thisAvg) ? null : roundOne(thisAvg - prevAvg))
                    .build());
        }
        if (!haveComparison) {
            log.debug("[weekly-report] user {} has no week-over-week overlap, skipping", userId);
            return false;
        }

        String summary = buildSummary(deltas, all.size());
        notificationService.push(
                userId,
                "WEEKLY_REPORT",
                "Your weekly interview recap",
                summary,
                "/pages/interview/history"
        );
        try {
            wechatSubscribeService.sendWeeklyReport(userId, summary);
        } catch (Exception e) {
            log.warn("[weekly-report] wx subscribe push failed for user {}: {}", userId, e.toString());
        }
        return true;
    }

    /** Ask Qwen for a short, concrete recap. Falls back to a deterministic summary on AI failure. */
    private String buildSummary(Map<String, DimensionDelta> deltas, int interviewCount) {
        String factSheet = buildFactSheet(deltas, interviewCount);
        String prompt = "You are a career coach writing a short weekly recap for a candidate using a mock-interview app. " +
                "Use the facts below to praise one improvement and call out one focus area. " +
                "Reply in English, plain text, under 60 words, no markdown, no lists.\n\nFacts:\n" + factSheet;
        try {
            String ai = aiService.chat(prompt);
            if (ai != null && !ai.isBlank()) return ai.trim();
        } catch (Exception e) {
            log.warn("[weekly-report] AI summary failed, falling back to template: {}", e.toString());
        }
        return fallbackSummary(deltas, interviewCount);
    }

    private String buildFactSheet(Map<String, DimensionDelta> deltas, int interviewCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Interviews completed in last 14 days: ").append(interviewCount).append('\n');
        for (DimensionDelta d : deltas.values()) {
            if (d.getThisAvg() == null && d.getPreviousAvg() == null) continue;
            sb.append("- ").append(d.getName()).append(": ");
            sb.append("previous=").append(d.getPreviousAvg() == null ? "n/a" : d.getPreviousAvg());
            sb.append(", this=").append(d.getThisAvg() == null ? "n/a" : d.getThisAvg());
            if (d.getDelta() != null) {
                sb.append(", delta=").append(d.getDelta() >= 0 ? "+" : "").append(d.getDelta());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String fallbackSummary(Map<String, DimensionDelta> deltas, int interviewCount) {
        String biggestGain = pickExtreme(deltas, true);
        String biggestDrop = pickExtreme(deltas, false);
        StringBuilder sb = new StringBuilder("This week you wrapped ");
        sb.append(interviewCount).append(" mock interview").append(interviewCount == 1 ? "" : "s").append(". ");
        if (biggestGain != null) {
            sb.append("Biggest gain: ").append(biggestGain).append(". ");
        }
        if (biggestDrop != null) {
            sb.append("Watch out for: ").append(biggestDrop).append(". ");
        } else {
            sb.append("Keep going — every session adds clarity. ");
        }
        return sb.toString().trim();
    }

    private String pickExtreme(Map<String, DimensionDelta> deltas, boolean gain) {
        DimensionDelta picked = null;
        for (DimensionDelta d : deltas.values()) {
            if (d.getDelta() == null) continue;
            if (gain && d.getDelta() > 0 && (picked == null || d.getDelta() > picked.getDelta())) picked = d;
            if (!gain && d.getDelta() < 0 && (picked == null || d.getDelta() < picked.getDelta())) picked = d;
        }
        if (picked == null) return null;
        return String.format(Locale.ROOT, "%s %+.1f", humanize(picked.getName()), picked.getDelta());
    }

    private String humanize(String key) {
        if (key == null) return "";
        switch (key) {
            case "technical": return "technical";
            case "communication": return "communication";
            case "logic": return "logic";
            case "expression": return "expression";
            case "pressureResistance": return "pressure resistance";
            default: return key;
        }
    }

    private JsonNode parseRadar(String reportJson) {
        if (reportJson == null || reportJson.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(reportJson);
            return root.get("radarChart");
        } catch (Exception e) {
            log.debug("[weekly-report] could not parse report json: {}", e.toString());
            return null;
        }
    }

    private double roundOne(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionDelta {
        private String name;
        private Double previousAvg;
        private Double thisAvg;
        /** thisAvg - previousAvg; null when one side missing. */
        private Double delta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunSummary {
        private int totalCandidates;
        private int delivered;
        private int skipped;
        private LocalDateTime runAt;
    }
}
