package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.career.model.entity.WxSubscribeQuota;
import com.group1.career.repository.UserAuthRepository;
import com.group1.career.repository.WxSubscribeQuotaRepository;
import com.group1.career.service.WechatSubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * F10: WechatSubscribeServiceImpl — access token (Redis-cached, 2 h TTL) + subscribe push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatSubscribeServiceImpl implements WechatSubscribeService {

    private static final String REDIS_TOKEN_KEY = "wx:access_token";
    private static final String TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String SEND_URL =
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";
    private static final String IDENTITY_TYPE_WECHAT = "WECHAT";

    @Value("${wechat.miniapp.appid}")
    private String appId;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    @Value("${wechat.subscribe.tpl-weekly-report:}")
    private String tplWeeklyReport;

    @Value("${wechat.subscribe.tpl-interview-report:}")
    private String tplInterviewReport;

    @Value("${wechat.subscribe.tpl-assessment:}")
    private String tplAssessment;

    @Value("${wechat.subscribe.tpl-ai-proactive:}")
    private String tplAiProactive;

    private final StringRedisTemplate redisTemplate;
    private final WxSubscribeQuotaRepository quotaRepository;
    private final UserAuthRepository userAuthRepository;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ─── Access Token ──────────────────────────────────────────────────────

    /**
     * Returns a valid access token. Fetches from Redis cache first; if missing
     * or expired, calls the WeChat API and caches the result for 1 h 50 min
     * (token is valid for 2 h — we refresh 10 min early to avoid edge-case expiry).
     */
    String getAccessToken() {
        String cached = redisTemplate.opsForValue().get(REDIS_TOKEN_KEY);
        if (cached != null && !cached.isBlank()) return cached;

        try {
            String url = String.format(TOKEN_URL, appId, secret);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(res.body());
            if (node.has("access_token")) {
                String token = node.get("access_token").asText();
                redisTemplate.opsForValue().set(REDIS_TOKEN_KEY, token, 110, TimeUnit.MINUTES);
                return token;
            }
            log.error("[WxSubscribe] Failed to get access token: {}", res.body());
        } catch (Exception e) {
            log.error("[WxSubscribe] Exception fetching access token: {}", e.getMessage(), e);
        }
        return null;
    }

    // ─── Quota Management ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void recordGrant(Long userId, Map<String, String> accepted) {
        if (accepted == null || accepted.isEmpty()) return;
        accepted.forEach((templateId, result) -> {
            if (!"accept".equalsIgnoreCase(result)) return;
            Optional<WxSubscribeQuota> opt = quotaRepository.findByUserIdAndTemplateId(userId, templateId);
            if (opt.isPresent()) {
                WxSubscribeQuota quota = opt.get();
                quota.setRemaining(quota.getRemaining() + 1);
                quota.setUpdatedAt(LocalDateTime.now());
                quotaRepository.save(quota);
            } else {
                quotaRepository.save(WxSubscribeQuota.builder()
                        .userId(userId)
                        .templateId(templateId)
                        .remaining(1)
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        });
        log.debug("[WxSubscribe] recordGrant userId={} templates={}", userId, accepted.keySet());
    }

    // ─── Send ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public boolean send(Long userId, String templateId, String page, Map<String, String> data) {
        if (templateId == null || templateId.isBlank()) {
            log.debug("[WxSubscribe] Skipping send: template ID not configured for userId={}", userId);
            return false;
        }

        // Decrement quota atomically
        int updated = quotaRepository.decrementRemaining(userId, templateId);
        if (updated == 0) {
            log.debug("[WxSubscribe] No quota for userId={} templateId={}", userId, templateId);
            return false;
        }

        // Resolve openid
        Optional<String> openidOpt = userAuthRepository.findByUserId(userId).stream()
                .filter(a -> IDENTITY_TYPE_WECHAT.equals(a.getIdentityType()))
                .map(a -> a.getIdentifier())
                .findFirst();
        if (openidOpt.isEmpty()) {
            log.debug("[WxSubscribe] No WeChat openid for userId={}", userId);
            return false;
        }

        String token = getAccessToken();
        if (token == null) return false;

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("touser", openidOpt.get());
            body.put("template_id", templateId);
            if (page != null && !page.isBlank()) body.put("page", page);
            ObjectNode dataNode = body.putObject("data");
            if (data != null) {
                data.forEach((k, v) -> {
                    ObjectNode field = objectMapper.createObjectNode();
                    field.put("value", v);
                    dataNode.set(k, field);
                });
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(SEND_URL, token)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode resp = objectMapper.readTree(res.body());
            int errcode = resp.path("errcode").asInt(-1);
            if (errcode == 0) {
                log.info("[WxSubscribe] Sent templateId={} to userId={}", templateId, userId);
                return true;
            }
            log.warn("[WxSubscribe] Send failed userId={} templateId={} errcode={} errmsg={}",
                    userId, templateId, errcode, resp.path("errmsg").asText());
        } catch (Exception e) {
            log.error("[WxSubscribe] Exception sending subscribe msg userId={}: {}", userId, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean sendWeeklyReport(Long userId, String summaryText) {
        return send(userId, tplWeeklyReport, "/pages/home/index",
                Map.of("thing1", summaryText == null ? "本周学习报告已生成" : summaryText,
                       "time2", LocalDateTime.now().toLocalDate().toString()));
    }

    @Override
    public boolean sendInterviewReport(Long userId, int score, String position) {
        return send(userId, tplInterviewReport, "/pages/interview/history",
                Map.of("thing1", position == null ? "模拟面试" : position,
                       "number2", String.valueOf(score)));
    }

    @Override
    public boolean sendAssessmentResult(Long userId, String scaleTitle, String result) {
        return send(userId, tplAssessment, "/pages/assessment/result",
                Map.of("thing1", scaleTitle == null ? "测评完成" : scaleTitle,
                       "thing2", result == null ? "点击查看详情" : result));
    }

    @Override
    public boolean sendAiProactive(Long userId, String messageText) {
        return send(userId, tplAiProactive, "/pages/assistant/index",
                Map.of("thing1", messageText == null ? "AI助手有新消息" : messageText));
    }
}
