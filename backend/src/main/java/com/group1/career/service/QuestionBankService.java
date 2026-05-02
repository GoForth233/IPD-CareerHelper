package com.group1.career.service;

import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Crowd-sourced interview question bank.
 *
 * <p>Two responsibilities:
 * <ol>
 *   <li>Serve a paged/filtered list to the {@code /pages/market} UI and
 *       process a "like" tap.</li>
 *   <li>Provide a probabilistic question draw to the interview greeting
 *       flow — when the bank has fitting questions for the candidate's
 *       (position, difficulty), we use a real one ~50% of the time so
 *       repeat candidates eventually see new angles.</li>
 * </ol>
 *
 * <p>Hashing of the contributor userId is intentionally simple — SHA-256 of
 * the decimal id with a peppered prefix. This is anonymity for the public
 * list endpoint, not crypto-grade k-anonymity (the user-space is small);
 * if we ever need stronger guarantees we can wire in a per-deployment salt.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService {

    /** Probability of drawing a real question instead of a generic angle. */
    private static final double DRAW_PROBABILITY = 0.5;
    private static final int DRAW_POOL_SIZE = 8;
    private static final int MIN_CONTENT_CHARS = 8;
    private static final int MAX_CONTENT_CHARS = 1000;

    private final InterviewQuestionRepository repo;

    /** Provider injected so tests can stub it; production uses a default seed. */
    private final Random rng = new Random();

    @Value("${app.questionbank.pepper:careerloop-qbank}")
    private String pepper;

    /** Page through the bank with optional filters. Sorting is "popular": likes desc, createdAt desc. */
    @Transactional(readOnly = true)
    public Page<InterviewQuestion> list(String position, String difficulty, int page, int size) {
        return list(position, difficulty, null, page, size);
    }

    /** Page through the bank with optional filters including source (OFFICIAL / USER / AI_GENERATED). */
    @Transactional(readOnly = true)
    public Page<InterviewQuestion> list(String position, String difficulty, String source, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(50, Math.max(1, size));
        Sort sort = Sort.by(Sort.Direction.DESC, "likes")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.search(blankToNull(position), blankToNull(difficulty), blankToNull(source),
                PageRequest.of(safePage, safeSize, sort));
    }

    /**
     * Persist a contribution. Returns the saved row. Throws nothing fatal
     * on validation failure — caller is the controller, which will surface
     * a friendly message; we still raise IllegalArgumentException so unit
     * tests can assert the exact path.
     */
    @Transactional
    public InterviewQuestion contribute(Long contributorUserId, String position, String difficulty,
                                        String content, String summary) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.length() < MIN_CONTENT_CHARS) {
            throw new IllegalArgumentException("Question is too short. Add a few more words so others can answer it.");
        }
        if (trimmed.length() > MAX_CONTENT_CHARS) {
            trimmed = trimmed.substring(0, MAX_CONTENT_CHARS);
        }
        InterviewQuestion saved = repo.save(InterviewQuestion.builder()
                .position(blankToValue(position, "General"))
                .difficulty(normalizeDifficulty(difficulty))
                .content(trimmed)
                .summary(blankToNull(summary))
                .contributorHash(hashUserId(contributorUserId))
                .likes(0)
                .drawCount(0)
                .status("APPROVED")
                .build());
        log.info("[qbank] contribution saved id={} position={} difficulty={}",
                saved.getId(), saved.getPosition(), saved.getDifficulty());
        return saved;
    }

    /**
     * Bump the like counter. Idempotent-ish: we don't track per-user votes,
     * so a user can like multiple times — by design (the bar is "rough
     * popularity", not authoritative). Frontend disables the button after
     * tap to avoid accidental double-likes from a fat finger.
     */
    @Transactional
    public Optional<InterviewQuestion> like(Long questionId) {
        int updated = repo.incrementLikes(questionId);
        if (updated == 0) return Optional.empty();
        return repo.findById(questionId);
    }

    /**
     * Maybe draw a question for the interview greeting. Returns
     * {@link Optional#empty()} for the generic-angle path; populated value
     * means "use this content, and we already bumped its draw_count".
     */
    @Transactional
    public Optional<InterviewQuestion> drawForInterview(String position, String difficulty) {
        if (rng.nextDouble() >= DRAW_PROBABILITY) return Optional.empty();
        try {
            List<InterviewQuestion> pool = repo.drawPool(
                    blankToValue(position, "General"),
                    normalizeDifficulty(difficulty),
                    PageRequest.of(0, DRAW_POOL_SIZE));
            if (pool.isEmpty()) return Optional.empty();
            InterviewQuestion picked = pool.get(rng.nextInt(pool.size()));
            try {
                repo.incrementDrawCount(picked.getId());
            } catch (DataAccessException e) {
                log.debug("[qbank] draw_count bump failed for id={}: {}", picked.getId(), e.toString());
            }
            return Optional.of(picked);
        } catch (DataAccessException e) {
            log.warn("[qbank] drawForInterview failed (falling back to generic): {}", e.toString());
            return Optional.empty();
        }
    }

    private String hashUserId(Long uid) {
        if (uid == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((pepper + ":" + uid).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is part of every JDK we'd ever run on; if it's missing
            // we'd have far worse problems than an anonymous question bank.
            log.warn("[qbank] SHA-256 unavailable, falling back to plain id: {}", e.toString());
            return "uid-" + uid;
        }
    }

    private String normalizeDifficulty(String raw) {
        if (raw == null || raw.isBlank()) return "Normal";
        String t = raw.trim();
        if (t.equalsIgnoreCase("easy")) return "Easy";
        if (t.equalsIgnoreCase("hard")) return "Hard";
        return "Normal";
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String blankToValue(String s, String def) {
        return (s == null || s.isBlank()) ? def : s.trim();
    }
}
