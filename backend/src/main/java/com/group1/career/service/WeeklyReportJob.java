package com.group1.career.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron entry for {@link WeeklyReportService}. Runs Monday 09:00 server time
 * (Asia/Shanghai). The job itself is idempotent enough — re-running on the
 * same day produces the same notification copy with a fresh row, which is
 * acceptable because users only get one weekly recap per week in normal
 * operation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app.weekly-report")
public class WeeklyReportJob {

    private final WeeklyReportService weeklyReportService;

    @Scheduled(cron = "0 0 9 ? * MON", zone = "Asia/Shanghai")
    public void runWeekly() {
        log.info("[weekly-report] cron tick, building reports");
        WeeklyReportService.RunSummary summary = weeklyReportService.runForAll();
        log.info("[weekly-report] cron run complete: candidates={} delivered={} skipped={}",
                summary.getTotalCandidates(), summary.getDelivered(), summary.getSkipped());
    }
}
