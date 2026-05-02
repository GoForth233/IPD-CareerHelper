package com.group1.career.service.impl;

import com.group1.career.service.ContentSafetyService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * F29: Content safety implementation using a local sensitive-word list.
 *
 * <p>The local filter catches the most common Chinese UGC violations
 * (spam links, gambling/fraud terms, political attack phrases) without
 * any external API call — fast, free, always available.</p>
 *
 * <p>Extend by setting {@code content-safety.aliyun-enabled=true} in prod
 * to layer in Aliyun Green text moderation on top of the local filter.
 * That integration can be wired later without changing this interface.</p>
 */
@Slf4j
@Service
public class ContentSafetyServiceImpl implements ContentSafetyService {

    @Value("${content-safety.enabled:true}")
    private boolean enabled;

    /** Patterns that unconditionally block UGC. Lower-cased at startup. */
    private static final List<String> BLOCKED_PATTERNS = Arrays.asList(
            // Gambling / fraud
            "博彩", "赌博", "赌场", "彩票平台", "快速致富", "一夜暴富",
            "洗钱", "诈骗", "传销", "虚假发票", "套现",
            // Explicit / adult
            "色情", "裸聊", "约炮", "援交", "外围",
            // Drug
            "毒品", "大麻", "可卡因", "冰毒",
            // Spam links
            "微信号加我", "私信我", "点击领取", "扫码领红包",
            // Violence / threats
            "杀人", "爆炸物", "制作炸弹",
            // Prohibited political (keep minimal — only egregious slurs)
            "法轮功", "六四真相"
    );

    private List<String> lowerPatterns;

    @PostConstruct
    public void init() {
        lowerPatterns = BLOCKED_PATTERNS.stream()
                .map(String::toLowerCase)
                .toList();
    }

    @Override
    public ContentCheckResult check(String text) {
        if (!enabled || text == null || text.isBlank()) {
            return ContentCheckResult.ok();
        }

        String lower = text.toLowerCase();
        for (String pattern : lowerPatterns) {
            if (lower.contains(pattern)) {
                log.info("[F29] content blocked — matched pattern: '{}' in text snippet: '{}'",
                        pattern, excerpt(text, 60));
                return ContentCheckResult.blocked("内容包含不当信息，请修改后重新提交");
            }
        }
        return ContentCheckResult.ok();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private static String excerpt(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}
