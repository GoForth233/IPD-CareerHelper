package com.group1.career.service.impl;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.exception.BizException;
import com.group1.career.service.FileService;
import com.group1.career.service.VoiceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * DashScope-backed voice pipeline used by the digital-human interviewer.
 *
 * <h3>ASR — paraformer-realtime-v2 (non-streaming form)</h3>
 * The mini-program records a single short utterance (<=60s) and POSTs the
 * file to us. We dump the bytes to a tmp file and let the SDK upload it to
 * the recogniser in one shot — far simpler than wiring the streaming
 * sendAudioFrame loop and well within latency budget for short answers.
 *
 * <h3>TTS — cosyvoice-v2</h3>
 * Synchronous {@code SpeechSynthesizer.call(text)} returns a complete mp3
 * blob (~24 kbps mono). We upload it to OSS under {@code tts/} and let the
 * caller presign a URL. mp3 is universally playable in the WeChat
 * mini-program {@code <audio>} component without any decoder bridging.
 *
 * <h3>Why a tmp file for ASR but not TTS?</h3>
 * The SDK's {@code Recognition.call(param, File)} is the cleanest blocking
 * API; the alternative is the streaming callback model which adds 50+ lines
 * for no quality win on a one-shot upload. TTS already returns a single
 * ByteBuffer so there's nothing to spool through disk.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceServiceImpl implements VoiceService {

    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.ai.api-key}")
    private String apiKey;

    @Value("${aliyun.voice.asr-model:paraformer-realtime-v2}")
    private String asrModel;

    @Value("${aliyun.voice.tts-model:cosyvoice-v2}")
    private String ttsModel;

    @Value("${aliyun.voice.tts-voice:longxiaochun_v2}")
    private String ttsVoice;

    /** Formats the SDK + Paraformer accept. Anything else gets rejected up-front. */
    private static final Set<String> ALLOWED_FORMATS =
            Set.of("mp3", "wav", "aac", "opus", "pcm", "amr", "speex");

    @PostConstruct
    void init() {
        // Beijing region. The SDK defaults to this URL too, but pinning it
        // avoids surprises if Alibaba ever bumps the SDK default.
        Constants.baseWebsocketApiUrl = "wss://dashscope.aliyuncs.com/api-ws/v1/inference";
        log.info("VoiceService init: asrModel={}, ttsModel={}, ttsVoice={}",
                asrModel, ttsModel, ttsVoice);
    }

    @Override
    public String transcribe(byte[] audioBytes, String format) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new BizException("Audio is empty");
        }
        String fmt = format == null ? "mp3" : format.trim().toLowerCase();
        if (!ALLOWED_FORMATS.contains(fmt)) {
            throw new BizException("Unsupported audio format: " + fmt);
        }

        File temp = null;
        Recognition recognizer = new Recognition();
        try {
            temp = File.createTempFile("asr-", "." + fmt);
            try (FileOutputStream fos = new FileOutputStream(temp)) {
                fos.write(audioBytes);
            }

            RecognitionParam param = RecognitionParam.builder()
                    .apiKey(apiKey)
                    .model(asrModel)
                    .format(fmt)
                    // 16kHz matches what the WeChat recorder produces; if the
                    // upstream ever changes we'll surface a clear error from
                    // the SDK rather than silently degrade transcript quality.
                    .sampleRate(16000)
                    .parameter("language_hints", new String[]{"en", "zh"})
                    .build();

            long t0 = System.currentTimeMillis();
            String raw = recognizer.call(param, temp);
            log.info("[asr] {} ms, {} bytes, raw={}", System.currentTimeMillis() - t0,
                    audioBytes.length, raw == null ? "null" : raw.substring(0, Math.min(120, raw.length())));
            return extractTranscriptText(raw);
        } catch (Exception e) {
            log.error("ASR failed", e);
            throw new BizException("Speech recognition failed: " + e.getMessage());
        } finally {
            // Closing the duplex API releases the underlying WebSocket; the
            // SDK keeps it open by default so it can be reused, but we want
            // the connection torn down so a stale socket can't carry over.
            try { recognizer.getDuplexApi().close(1000, "bye"); } catch (Exception ignored) {}
            if (temp != null && !temp.delete()) {
                log.debug("tmp ASR file not deleted: {}", temp.getAbsolutePath());
            }
        }
    }

    /**
     * Paraformer returns the full recognition JSON, e.g.:
     * {"sentences":[{"text":"你好","begin_time":0,"end_time":800,"words":[...]},...]}.
     * We only want the plain-text transcript for downstream use.
     * Falls back to the raw string if parsing fails so we never swallow speech silently.
     */
    private String extractTranscriptText(String raw) {
        if (raw == null || raw.isBlank()) return "";
        if (!raw.trim().startsWith("{")) return raw.trim();
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode sentences = root.path("sentences");
            if (sentences.isArray() && !sentences.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode s : sentences) {
                    String t = s.path("text").asText("").trim();
                    if (!t.isEmpty()) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(t);
                    }
                }
                String text = sb.toString().trim();
                if (!text.isEmpty()) return text;
            }
        } catch (Exception e) {
            log.warn("[asr] JSON parse failed, returning raw result: {}", e.getMessage());
        }
        return raw.trim();
    }

    @Override
    public TtsResult synthesize(String text) {
        if (text == null || text.isBlank()) {
            throw new BizException("TTS text is empty");
        }
        // CosyVoice hard-caps a single call at 20 000 chars; an interviewer
        // turn is always two sentences, so the cap is academic. Defensive
        // truncate just in case a future prompt forgets the brevity hint.
        String clipped = text.length() > 4000 ? text.substring(0, 4000) : text;

        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model(ttsModel)
                .voice(ttsVoice)
                .build();
        // Per docs: re-instantiate the synthesizer for every call; it isn't
        // thread-safe and reusing it across calls leaks WebSocket state.
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
        try {
            long t0 = System.currentTimeMillis();
            ByteBuffer audio = synthesizer.call(clipped);
            long elapsed = System.currentTimeMillis() - t0;
            if (audio == null || audio.remaining() == 0) {
                throw new BizException("TTS returned empty audio");
            }
            byte[] bytes = new byte[audio.remaining()];
            audio.get(bytes);

            String filename = "tts-" + System.currentTimeMillis() + ".mp3";
            String key = fileService.uploadBytes(bytes, filename, "tts");

            // CosyVoice mp3 default is mono ~24 kbps. The estimate is only
            // used by the digital-human mouth animation as a fallback timer
            // before <audio> reports loadedmetadata duration; even a 30%
            // error is invisible to the user.
            long durationMs = Math.max(1000L, (long) bytes.length * 8L / 24L);
            log.info("[tts] {} ms, {} chars -> {} bytes (~{} ms audio), key={}",
                    elapsed, clipped.length(), bytes.length, durationMs, key);
            return new TtsResult(key, durationMs);
        } catch (Exception e) {
            log.error("TTS failed", e);
            throw new BizException("Speech synthesis failed: " + e.getMessage());
        } finally {
            try { synthesizer.getDuplexApi().close(1000, "bye"); } catch (Exception ignored) {}
        }
    }
}
