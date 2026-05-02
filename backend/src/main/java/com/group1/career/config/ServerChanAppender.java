package com.group1.career.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * F27: Logback appender that fires a ServerChan (server酷) push notification
 * whenever an ERROR-or-above log event is emitted.
 *
 * <p>Configured via logback-spring.xml. The {@code sendKey} is injected from
 * the Spring Environment via the {@code <springProperty>} mechanism.
 * Rate-limited to one push per 60 seconds per JVM to prevent alert storms.</p>
 *
 * <p>Uses Java 11 HttpClient (already on the classpath) — no extra
 * dependency required.</p>
 */
public class ServerChanAppender extends AppenderBase<ILoggingEvent> {

    @Setter
    private String sendKey = "";

    @Setter
    private boolean enabled = true;

    private static final String API_TEMPLATE =
            "https://sctapi.ftqq.com/%s.send?title=%s&desp=%s";

    private static final long THROTTLE_MS = 60_000L;
    private final AtomicLong lastSentAt = new AtomicLong(0);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    protected void append(ILoggingEvent event) {
        if (!enabled || sendKey == null || sendKey.isBlank()) return;
        if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            long now = System.currentTimeMillis();
            if (now - lastSentAt.get() < THROTTLE_MS) return;
            lastSentAt.set(now);
            sendAsync(event);
        }
    }

    private void sendAsync(ILoggingEvent event) {
        try {
            String title = encode("[CareerLoop ERROR] " + event.getLoggerName());
            String body = encode(
                    "**Level**: " + event.getLevel() + "\n\n" +
                    "**Logger**: " + event.getLoggerName() + "\n\n" +
                    "**Message**: " + event.getFormattedMessage() + "\n\n" +
                    "**Thread**: " + event.getThreadName()
            );
            String url = String.format(API_TEMPLATE, sendKey, title, body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            httpClient.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            addWarn("[ServerChanAppender] failed to send alert: " + e.getMessage());
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
