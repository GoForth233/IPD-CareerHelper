package com.group1.career.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Minimal Bilibili web search client.
 *
 * <p>Bilibili's public {@code wbi/search/type} endpoint requires a wbi
 * signature ({@code w_rid} + {@code wts}) in the query string. The signing
 * keys themselves come from {@code /x/web-interface/nav}'s {@code img_url}
 * and {@code sub_url} basenames mixed in a fixed permutation (the "mixin
 * key"). We cache the resolved mixin key for an hour — Bilibili rotates it
 * roughly daily.</p>
 *
 * <p>If anything goes wrong (B站 rate-limits us, the algorithm changes,
 * the network blips) we return an empty list and let the daily refresh
 * job log + try again tomorrow. The home feed gracefully shows whatever
 * was last successfully cached.</p>
 */
@Slf4j
@Component
public class BilibiliClient {

    private static final String NAV_URL = "https://api.bilibili.com/x/web-interface/nav";
    private static final String SEARCH_URL = "https://api.bilibili.com/x/web-interface/wbi/search/type";

    private static final String UA =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0 Safari/537.36";

    /**
     * The fixed permutation of wbi key bytes that Bilibili's web client uses
     * to derive the 32-byte mixin key. Reverse-engineered from B站 web JS;
     * stable since 2023 — but treat it as opaque external knowledge.
     */
    private static final int[] MIXIN_KEY_ENCODE_TAB = {
            46, 47, 18,  2, 53,  8, 23, 32, 15, 50, 10, 31, 58,  3, 45, 35,
            27, 43,  5, 49, 33,  9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
            37, 48,  7, 16, 24, 55, 40, 61, 26, 17,  0,  1, 60, 51, 30,  4,
            22, 25, 54, 21, 56, 59,  6, 63, 57, 62, 11, 36, 20, 34, 44, 52
    };

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /** Cached mixin key + the wall-clock at which it must be re-fetched. */
    private final AtomicReference<MixinKeyCache> mixinKeyCache = new AtomicReference<>();

    /**
     * Run a video search and return the first {@code limit} results. The
     * call returns an empty list rather than throwing on any failure — the
     * caller (a scheduled job) re-tries on its own cron, so swallowing keeps
     * a transient B站 hiccup out of our boot logs.
     */
    public List<VideoHit> searchVideos(String keyword, int limit) {
        try {
            String mixin = resolveMixinKey();
            if (mixin == null) {
                log.warn("[bili] could not resolve wbi mixin key — skipping search '{}'", keyword);
                return List.of();
            }

            Map<String, String> params = new TreeMap<>();
            params.put("search_type", "video");
            params.put("keyword", keyword);
            params.put("page", "1");
            params.put("page_size", String.valueOf(Math.max(1, Math.min(limit, 50))));
            params.put("order", "totalrank");
            // 一些可选过滤位用默认值；Bilibili 风控对这几个空值很宽容。
            params.put("platform", "pc");
            params.put("wts", String.valueOf(System.currentTimeMillis() / 1000L));

            String query = signQuery(params, mixin);
            URI uri = URI.create(SEARCH_URL + "?" + query);

            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("User-Agent", UA)
                    .header("Referer", "https://www.bilibili.com/")
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                log.warn("[bili] search '{}' HTTP {}: {}", keyword, resp.statusCode(),
                        resp.body() == null ? "" : resp.body().substring(0, Math.min(200, resp.body().length())));
                return List.of();
            }

            JsonNode root = mapper.readTree(resp.body());
            int code = root.path("code").asInt(-1);
            if (code != 0) {
                log.warn("[bili] search '{}' API code={} msg={}", keyword, code, root.path("message").asText());
                return List.of();
            }

            JsonNode result = root.path("data").path("result");
            if (!result.isArray()) return List.of();

            List<VideoHit> out = new ArrayList<>();
            for (JsonNode n : result) {
                String bvid = n.path("bvid").asText("");
                if (bvid.isBlank()) continue;
                out.add(VideoHit.builder()
                        .bvid(bvid)
                        .title(stripEmTags(n.path("title").asText("")))
                        .coverUrl(forceHttps(n.path("pic").asText("")))
                        .upName(n.path("author").asText(""))
                        .upMid(n.path("mid").asLong())
                        .durationSec(parseDuration(n.path("duration").asText("0:00")))
                        .viewCount(n.path("play").asLong())
                        .build());
                if (out.size() >= limit) break;
            }
            return out;
        } catch (Exception e) {
            log.warn("[bili] search '{}' failed: {}", keyword, e.toString());
            return List.of();
        }
    }

    // ============================================================
    //   wbi key resolution + signing
    // ============================================================

    private String resolveMixinKey() {
        MixinKeyCache c = mixinKeyCache.get();
        if (c != null && c.expiresAt > System.currentTimeMillis()) {
            return c.key;
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(NAV_URL))
                    .header("User-Agent", UA)
                    .header("Referer", "https://www.bilibili.com/")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;
            JsonNode root = mapper.readTree(resp.body());
            JsonNode wbi = root.path("data").path("wbi_img");
            String img = baseName(wbi.path("img_url").asText(""));
            String sub = baseName(wbi.path("sub_url").asText(""));
            if (img.isBlank() || sub.isBlank()) return null;
            String raw = img + sub;
            StringBuilder sb = new StringBuilder(32);
            for (int idx : MIXIN_KEY_ENCODE_TAB) {
                if (idx < raw.length()) sb.append(raw.charAt(idx));
                if (sb.length() == 32) break;
            }
            String mixin = sb.toString();
            mixinKeyCache.set(new MixinKeyCache(mixin, System.currentTimeMillis() + Duration.ofHours(1).toMillis()));
            return mixin;
        } catch (Exception e) {
            log.warn("[bili] nav fetch failed: {}", e.toString());
            return null;
        }
    }

    private static final Set<Character> WBI_FORBIDDEN_CHARS = Set.of('!', '\'', '(', ')', '*');

    /**
     * Encode + sort the params, append {@code w_rid = md5(query + mixin)}, and
     * return the final query string ready to drop after the URL's '?'.
     */
    private String signQuery(Map<String, String> params, String mixinKey) {
        // TreeMap already gives us alphabetical order.
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            String v = stripForbidden(e.getValue());
            String enc = URLEncoder.encode(v, StandardCharsets.UTF_8);
            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)).append('=').append(enc);
            first = false;
        }
        String query = sb.toString();
        String wrid = md5(query + mixinKey);
        return query + "&w_rid=" + wrid;
    }

    private static String stripForbidden(String v) {
        if (v == null) return "";
        StringBuilder out = new StringBuilder(v.length());
        for (char c : v.toCharArray()) {
            if (!WBI_FORBIDDEN_CHARS.contains(c)) out.append(c);
        }
        return out.toString();
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(32);
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("MD5 unavailable", e);
        }
    }

    /** Strip protocol + path, leaving e.g. {@code 7cd084941338484aae1ad9425b84077c}. */
    private static String baseName(String url) {
        if (url == null || url.isEmpty()) return "";
        int slash = url.lastIndexOf('/');
        String f = slash >= 0 ? url.substring(slash + 1) : url;
        int dot = f.lastIndexOf('.');
        return dot > 0 ? f.substring(0, dot) : f;
    }

    private static String stripEmTags(String html) {
        if (html == null) return "";
        // Bilibili wraps matched keywords with <em class="keyword">…</em>; strip
        // them so the title in our DB is clean text.
        return html.replaceAll("</?em[^>]*>", "");
    }

    private static String forceHttps(String url) {
        if (url == null) return null;
        if (url.startsWith("//")) return "https:" + url;
        if (url.startsWith("http://")) return "https://" + url.substring(7);
        return url;
    }

    /** Parse a "M:SS" or "MM:SS" duration string into seconds. */
    private static int parseDuration(String d) {
        if (d == null || d.isBlank()) return 0;
        try {
            String[] parts = d.split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            } else if (parts.length == 3) {
                return Integer.parseInt(parts[0]) * 3600
                        + Integer.parseInt(parts[1]) * 60
                        + Integer.parseInt(parts[2]);
            }
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VideoHit {
        private String bvid;
        private String title;
        private String coverUrl;
        private String upName;
        private Long upMid;
        private Integer durationSec;
        private Long viewCount;
    }

    private record MixinKeyCache(String key, long expiresAt) {}
}
