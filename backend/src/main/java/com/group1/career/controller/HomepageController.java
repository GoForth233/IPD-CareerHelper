package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.HomeArticle;
import com.group1.career.model.entity.HomeConsultation;
import com.group1.career.model.entity.HomeVideo;
import com.group1.career.repository.HomeArticleRepository;
import com.group1.career.repository.HomeConsultationRepository;
import com.group1.career.repository.HomeVideoRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.CareerService;
import com.group1.career.service.HomeContentRefreshJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Aggregated home feed: B站 videos (rotated daily), career articles,
 * career consultations, and a small set of career-path "spotlight" cards.
 *
 * <p>Rotation strategy: videos are sampled with {@code MySQL RAND(seed)}
 * where seed = {@code dayOfYear * 10000 + (userId mod 10000)}. This gives
 * each user a stable batch within a day and a fresh slice every morning,
 * even before the daily cron runs.</p>
 */
@Slf4j
@Tag(name = "Homepage API", description = "Aggregated data feed for the homepage")
@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private static final int VIDEO_LIMIT = 8;
    private static final int ARTICLE_LIMIT = 6;
    private static final int CONSULTATION_LIMIT = 3;
    private static final int CAREER_CARD_LIMIT = 4;

    private static final int REFRESH_RATE_LIMIT = 3;
    private static final long REFRESH_WINDOW_MINUTES = 5L;

    private final CareerService careerService;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final HomeVideoRepository homeVideoRepository;
    private final HomeArticleRepository homeArticleRepository;
    private final HomeConsultationRepository homeConsultationRepository;
    private final HomeContentRefreshJob homeContentRefreshJob;
    private final StringRedisTemplate redisTemplate;

    @Operation(summary = "Manually trigger a fresh Bilibili video pull (rate-limited: 3×/5 min per user)")
    @PostMapping("/refresh")
    public Result<String> triggerRefresh(@RequestParam(required = false) Long userId) {
        String key = "home:refresh:" + (userId != null ? "u:" + userId : "anon");
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, REFRESH_WINDOW_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count > REFRESH_RATE_LIMIT) {
            return Result.error(429, "刷新太频繁，请 5 分钟后再试");
        }
        CompletableFuture.runAsync(() -> {
            try {
                int written = homeContentRefreshJob.refresh();
                log.info("[home-refresh] manual trigger wrote {} new videos (user={})", written, userId);
            } catch (Exception e) {
                log.error("[home-refresh] manual trigger failed", e);
            }
        });
        return Result.success("内容刷新已触发");
    }

    @Operation(summary = "Get homepage aggregated feed (videos, articles, consultations, paths, stats)")
    @GetMapping("/feed")
    public Result<HomepageFeedDto> getHomepageFeed(@RequestParam(required = false) Long userId) {

        // 1. Bilibili videos — rotated by day so the same user sees a stable
        //    batch within a day but fresh content the next morning.
        long seed = todaysSeed(userId);
        List<HomeVideo> videos;
        try {
            videos = homeVideoRepository.sampleByRand(seed, VIDEO_LIMIT);
        } catch (Exception e) {
            // RAND(seed) isn't supported on H2 (used in tests); fall back to
            // a deterministic top-by-score listing so the test path still works.
            log.debug("[homepage] RAND() sampling unavailable, falling back to sort: {}", e.getMessage());
            videos = homeVideoRepository.findAllByOrderBySortScoreDesc(PageRequest.of(0, VIDEO_LIMIT));
        }
        List<VideoCardDto> videoCards = videos.stream()
                .map(this::toVideoCard)
                .collect(Collectors.toList());

        // 2. Articles — pinned first, then newest, excludes hidden.
        List<ArticleDto> articles = homeArticleRepository
                .findAllByHiddenFalseOrderByPinnedDescPublishedAtDescIdDesc(PageRequest.of(0, ARTICLE_LIMIT))
                .stream()
                .map(this::toArticleDto)
                .collect(Collectors.toList());

        // 3. Consultations — short tip cards, newest first.
        List<ConsultationDto> consultations = homeConsultationRepository
                .findAllByOrderByPublishedAtDescIdDesc(PageRequest.of(0, CONSULTATION_LIMIT))
                .stream()
                .map(this::toConsultationDto)
                .collect(Collectors.toList());

        // 4. Career path spotlight cards (kept for the Skill Map cross-link).
        List<CareerPath> paths = careerService.getAllPaths();
        List<CareerCardDto> careerCards = paths.stream()
                .sorted(Comparator.comparing(CareerPath::getPathId))
                .limit(CAREER_CARD_LIMIT)
                .map(p -> CareerCardDto.builder()
                        .pathId(p.getPathId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());

        // 5. Quick stats — live counts.
        QuickStatsDto stats = QuickStatsDto.builder()
                .totalCareerPaths(paths.size())
                .totalUsers((int) userRepository.count())
                .totalInterviews((int) interviewRepository.count())
                .build();

        HomepageFeedDto feed = HomepageFeedDto.builder()
                .videos(videoCards)
                .articles(articles)
                .consultations(consultations)
                .careerCards(careerCards)
                .stats(stats)
                .build();

        return Result.success(feed);
    }

    /**
     * Stable per-day, per-user random seed. {@code dayOfYear} guarantees the
     * batch rotates daily; mixing in {@code userId} keeps two users on the
     * same day from seeing the identical sample, which makes the home page
     * feel personalised even though we aren't running real recommendations.
     */
    private long todaysSeed(Long userId) {
        int day = LocalDate.now().getDayOfYear();
        long uid = userId == null ? 0L : Math.abs(userId % 10_000);
        return ((long) day) * 10_000L + uid;
    }

    private VideoCardDto toVideoCard(HomeVideo v) {
        return VideoCardDto.builder()
                .id(v.getId())
                .bvid(v.getBvid())
                .title(v.getTitle())
                .coverUrl(v.getCoverUrl())
                .upName(v.getUpName())
                .durationSec(v.getDurationSec())
                .viewCount(v.getViewCount())
                .keyword(v.getKeyword())
                // Frontend opens this URL in <web-view> (requires *.bilibili.com
                // on the WeChat MP business-domain whitelist).
                .url("https://www.bilibili.com/video/" + v.getBvid())
                .build();
    }

    private ArticleDto toArticleDto(HomeArticle a) {
        return ArticleDto.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .imageUrl(a.getImageUrl())
                .url(a.getSourceUrl())
                .category(a.getCategory())
                .build();
    }

    private ConsultationDto toConsultationDto(HomeConsultation c) {
        return ConsultationDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .body(c.getBodyMd())
                .author(c.getAuthor())
                .imageUrl(c.getImageUrl())
                .build();
    }

    // ================= DTO Classes =================

    @Data @Builder @AllArgsConstructor
    public static class HomepageFeedDto {
        private List<VideoCardDto> videos;
        private List<ArticleDto> articles;
        private List<ConsultationDto> consultations;
        private List<CareerCardDto> careerCards;
        private QuickStatsDto stats;
    }

    @Data @Builder @AllArgsConstructor
    public static class VideoCardDto {
        private Long id;
        private String bvid;
        private String title;
        private String coverUrl;
        private String upName;
        private Integer durationSec;
        private Long viewCount;
        private String keyword;
        private String url;
    }

    @Data @Builder @AllArgsConstructor
    public static class ArticleDto {
        private Long id;
        private String title;
        private String summary;
        private String imageUrl;
        private String url;
        private String category;
    }

    @Data @Builder @AllArgsConstructor
    public static class ConsultationDto {
        private Long id;
        private String title;
        private String body;
        private String author;
        private String imageUrl;
    }

    @Data @Builder @AllArgsConstructor
    public static class CareerCardDto {
        private Integer pathId;
        private String name;
        private String description;
    }

    @Data @Builder @AllArgsConstructor
    public static class QuickStatsDto {
        private int totalCareerPaths;
        private int totalUsers;
        private int totalInterviews;
    }
}
