package com.group1.career.service;

import com.group1.career.model.entity.HomeVideo;
import com.group1.career.repository.HomeVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pulls a fresh batch of career-related Bilibili videos every morning at
 * 03:00 server local time. Why 03:00? Bilibili's CDN is least loaded around
 * then; we also avoid colliding with the daily DB backup at 02:00.
 *
 * <p>If the table is empty on boot we trigger an immediate refresh on the
 * {@code ApplicationReadyEvent} so first-run dev environments don't have to
 * wait until 03:00 to see content.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeContentRefreshJob {

    /** Career-focused queries we rotate the home feed across. */
    private static final List<String> KEYWORDS = List.of(
            "面试", "求职面试", "Java 后端", "前端开发", "产品经理", "简历",
            "校招", "实习", "数据分析", "职业规划"
    );

    /** How many cards to pull per keyword per refresh. */
    private static final int PER_KEYWORD_LIMIT = 8;

    /** Drop videos older than this on every refresh — keeps the table tight. */
    private static final int RETENTION_DAYS = 30;

    private final BilibiliClient bilibili;
    private final HomeVideoRepository videoRepository;

    /** Cron: 03:00 every day, server timezone. */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledRefresh() {
        try {
            log.info("[home-refresh] scheduled run starting");
            int written = refresh();
            log.info("[home-refresh] scheduled run wrote {} new videos", written);
        } catch (Exception e) {
            log.error("[home-refresh] scheduled run failed", e);
        }
    }

    /**
     * If the table is empty (fresh deployment, dev environment), pull a batch
     * once the application is up so the home page isn't visibly empty.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmIfEmpty() {
        try {
            long n = videoRepository.count();
            if (n == 0) {
                log.info("[home-refresh] home_videos is empty, warming up");
                refresh();
            }
        } catch (Exception e) {
            // Don't fail boot if Bilibili is unreachable — the home page falls
            // back to careerCards + articles.
            log.warn("[home-refresh] warmup failed: {}", e.toString());
        }
    }

    /**
     * Run the refresh now. Public so an admin endpoint (Phase 7) can trigger
     * it on demand without waiting for the cron.
     */
    @Transactional
    public int refresh() {
        int written = 0;
        for (String kw : KEYWORDS) {
            try {
                List<BilibiliClient.VideoHit> hits = bilibili.searchVideos(kw, PER_KEYWORD_LIMIT);
                for (BilibiliClient.VideoHit h : hits) {
                    if (videoRepository.findByBvid(h.getBvid()).isPresent()) continue;
                    HomeVideo v = HomeVideo.builder()
                            .bvid(h.getBvid())
                            .title(h.getTitle())
                            .coverUrl(h.getCoverUrl())
                            .upName(h.getUpName())
                            .upMid(h.getUpMid())
                            .durationSec(h.getDurationSec())
                            .viewCount(h.getViewCount())
                            .keyword(kw)
                            .sortScore(h.getViewCount() == null ? 0L : h.getViewCount())
                            .build();
                    videoRepository.save(v);
                    written++;
                }
            } catch (Exception e) {
                log.warn("[home-refresh] keyword '{}' failed: {}", kw, e.toString());
            }
        }
        // Trim down old rows so the table never grows unbounded.
        try {
            int deleted = videoRepository.deleteOlderThan(LocalDateTime.now().minusDays(RETENTION_DAYS));
            if (deleted > 0) log.info("[home-refresh] retention pruned {} videos", deleted);
        } catch (Exception e) {
            log.warn("[home-refresh] retention pruning failed: {}", e.toString());
        }
        return written;
    }
}
