package com.group1.career.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Aggregates body-language scores per interview.
 *
 * <p>The frontend posts a base64 frame every ~2s during a voice interview;
 * we forward each frame to the Python sidecar (FastAPI + MediaPipe) and
 * keep the score in an in-memory ring keyed by interviewId. When the
 * interview ends, {@link #consume(Long)} returns the averaged dimension
 * scores so {@code InterviewReportController} can fold them into the
 * radarChart as a 6th dimension.</p>
 *
 * <p>Why in-memory? We're scoped to a single Spring Boot pod and the
 * window is short (a single interview, max 30 minutes). Persisting every
 * frame would be wasteful — the only durable artifact is the averaged
 * score that ends up in the cached report JSON.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BodyLanguageService {

    private final ObjectMapper objectMapper;

    @Value("${bodylang.sidecar-url:}")
    private String sidecarUrl;

    @Value("${bodylang.enabled:true}")
    private boolean enabled;

    /** Hard cap so a stuck client can't make us hold thousands of frames. */
    private static final int MAX_FRAMES_PER_INTERVIEW = 600;

    /** Per-interview score ring. ConcurrentHashMap so multiple sessions can ingest in parallel. */
    private final Map<Long, List<FrameScore>> buffers = new ConcurrentHashMap<>();

    /** Reused HTTP client; the sidecar is on the same compose network so latency is local. */
    private final AtomicReference<HttpClient> httpClient = new AtomicReference<>();

    /**
     * Send a frame to the sidecar and store the returned score. Best-effort —
     * any error is logged at debug and swallowed; we never want a flaky
     * sidecar to abort the interview itself.
     */
    public void recordFrame(Long interviewId, String frameBase64) {
        if (interviewId == null || frameBase64 == null || frameBase64.isBlank()) return;
        if (!enabled || sidecarUrl == null || sidecarUrl.isBlank()) {
            log.debug("[bodylang] disabled or sidecar URL missing, dropping frame for interview {}", interviewId);
            return;
        }
        try {
            FrameScore score = callSidecar(interviewId, frameBase64);
            if (score == null) return;
            buffers.compute(interviewId, (k, existing) -> {
                List<FrameScore> list = existing == null ? new ArrayList<>() : existing;
                if (list.size() < MAX_FRAMES_PER_INTERVIEW) list.add(score);
                return list;
            });
        } catch (Exception e) {
            log.debug("[bodylang] frame call failed for interview {}: {}", interviewId, e.toString());
        }
    }

    /**
     * Consume and clear the buffer. Returns the average per dimension or
     * {@code null} if no frames were collected.
     */
    public Aggregate consume(Long interviewId) {
        List<FrameScore> list = buffers.remove(interviewId);
        if (list == null || list.isEmpty()) return null;
        double eye = 0, expr = 0, posture = 0, conf = 0;
        for (FrameScore s : list) {
            eye += s.getEyeContact();
            expr += s.getExpression();
            posture += s.getPosture();
            conf += s.getConfidence();
        }
        int n = list.size();
        return Aggregate.builder()
                .eyeContact((int) Math.round(eye / n))
                .expression((int) Math.round(expr / n))
                .posture((int) Math.round(posture / n))
                .averageConfidence(conf / n)
                .frames(n)
                .bodyLanguage((int) Math.round((eye + expr + posture) / (3.0 * n)))
                .build();
    }

    /** Used by tests — drop a buffer without consuming so they don't leak between cases. */
    public void clear(Long interviewId) {
        buffers.remove(interviewId);
    }

    private FrameScore callSidecar(Long interviewId, String frameBase64) throws Exception {
        HttpClient client = httpClient.updateAndGet(c -> c == null
                ? HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build() : c);
        ObjectNode body = objectMapper.createObjectNode();
        body.put("frame_b64", frameBase64);
        body.put("interview_id", interviewId);
        HttpRequest req = HttpRequest.newBuilder(URI.create(sidecarUrl + "/analyze"))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            log.debug("[bodylang] sidecar returned {} for interview {}", res.statusCode(), interviewId);
            return null;
        }
        JsonNode node = objectMapper.readTree(res.body());
        return FrameScore.builder()
                .eyeContact(node.path("eye_contact").asInt(60))
                .expression(node.path("expression").asInt(60))
                .posture(node.path("posture").asInt(60))
                .confidence(node.path("confidence").asDouble(0.4))
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrameScore {
        private int eyeContact;
        private int expression;
        private int posture;
        private double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Aggregate {
        private int eyeContact;
        private int expression;
        private int posture;
        /** Convenience composite for the 6th radar dimension. */
        private int bodyLanguage;
        /** Average MediaPipe confidence — UI may want to label "low signal" runs. */
        private double averageConfidence;
        private int frames;

        public Map<String, Object> asMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("eyeContact", eyeContact);
            m.put("expression", expression);
            m.put("posture", posture);
            m.put("bodyLanguage", bodyLanguage);
            m.put("averageConfidence", Math.round(averageConfidence * 100.0) / 100.0);
            m.put("frames", frames);
            return m;
        }
    }
}
