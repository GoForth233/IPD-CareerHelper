package com.group1.career.job;

import com.group1.career.model.entity.HomeArticle;
import com.group1.career.repository.HomeArticleRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * F8: Scheduled RSS fetch job.
 *
 * <p>Runs every 6 hours. Fetches from Juejin and 36kr career feeds,
 * filters entries that match career-related keywords, and saves new
 * articles to {@code home_articles}. Duplicates are silently ignored
 * via the unique key on {@code source_url}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RssFetchJob {

    private static final String JUEJIN_RSS  = "https://juejin.cn/rss";
    private static final String KR36_RSS    = "https://36kr.com/feed";
    private static final int    MAX_PER_FEED = 20;
    private static final Set<String> CAREER_KEYWORDS = Set.of(
            "面试", "简历", "求职", "招聘", "职场", "跳槽", "薪资", "工资",
            "offer", "校招", "社招", "实习", "程序员", "开发者", "算法",
            "Java", "Python", "前端", "后端", "架构", "技术", "coding",
            "职业", "晋升", "升职", "职级", "互联网", "大厂", "字节", "阿里",
            "腾讯", "百度", "美团", "滴滴", "华为", "小米"
    );

    private final HomeArticleRepository homeArticleRepository;

    @Scheduled(cron = "0 0 */6 * * *")
    public void fetchAll() {
        log.info("[RssFetchJob] starting RSS fetch");
        int saved = 0;
        saved += fetchFeed(JUEJIN_RSS,  "RSS_JUEJIN",  "career");
        saved += fetchFeed(KR36_RSS,    "RSS_36KR",    "career");
        log.info("[RssFetchJob] done — {} new articles saved", saved);
    }

    private int fetchFeed(String feedUrl, String source, String defaultCategory) {
        int count = 0;
        try {
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed;
            try (XmlReader reader = new XmlReader(url.openStream())) {
                feed = input.build(reader);
            }
            List<SyndEntry> entries = feed.getEntries();
            for (int i = 0; i < Math.min(entries.size(), MAX_PER_FEED); i++) {
                SyndEntry entry = entries.get(i);
                String title = entry.getTitle();
                String link  = entry.getLink();
                if (title == null || link == null || link.isBlank()) continue;
                if (!matchesCareerKeyword(title + " " +
                        (entry.getDescription() != null ? entry.getDescription().getValue() : ""))) {
                    continue;
                }
                String summary = entry.getDescription() != null
                        ? truncate(entry.getDescription().getValue().replaceAll("<[^>]+>", ""), 300)
                        : null;
                LocalDateTime publishedAt = toLocalDateTime(entry.getPublishedDate());

                HomeArticle article = HomeArticle.builder()
                        .title(truncate(title, 255))
                        .summary(summary)
                        .sourceUrl(link)
                        .category(defaultCategory)
                        .source(source)
                        .publishedAt(publishedAt != null ? publishedAt : LocalDateTime.now())
                        .build();
                try {
                    homeArticleRepository.save(article); //NOSONAR null-safety: builder always populates required fields
                    count++;
                } catch (DataIntegrityViolationException dup) {
                    // duplicate source_url — already in DB, skip silently
                }
            }
        } catch (Exception e) {
            log.warn("[RssFetchJob] failed to fetch {}: {}", feedUrl, e.getMessage());
        }
        return count;
    }

    private boolean matchesCareerKeyword(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String kw : CAREER_KEYWORDS) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
