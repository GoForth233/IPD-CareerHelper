package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.dto.UserProfileSnapshot;
import com.group1.career.model.entity.User;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.UserProfileSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileSnapshotServiceImpl implements UserProfileSnapshotService {

    private static final DateTimeFormatter PROMPT_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public UserProfileSnapshot read(Long userId) {
        if (userId == null) return UserProfileSnapshot.builder().build();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return UserProfileSnapshot.builder().build();
        return parse(userOpt.get().getProfileSnapshot());
    }

    @Override
    public String renderForPrompt(Long userId) {
        UserProfileSnapshot snap = read(userId);
        if (snap == null || isEmpty(snap)) return "";

        List<String> lines = new ArrayList<>();
        lines.add("Known about this user (from prior tool usage — be specific, never claim ignorance of these):");

        if (snap.getAssessment() != null) {
            UserProfileSnapshot.AssessmentBlock a = snap.getAssessment();
            StringBuilder sb = new StringBuilder("- Assessment: ");
            if (a.getScaleTitle() != null) sb.append(a.getScaleTitle()).append(" → ");
            if (a.getSummary() != null) sb.append("type ").append(a.getSummary());
            if (a.getSuggestedRoles() != null && !a.getSuggestedRoles().isEmpty()) {
                sb.append("; AI-suggested roles: ").append(String.join(", ", a.getSuggestedRoles()));
            }
            if (a.getCompletedAt() != null) sb.append(" (").append(a.getCompletedAt().format(PROMPT_DATE_FMT)).append(")");
            lines.add(sb.toString());
        }

        if (snap.getResume() != null) {
            UserProfileSnapshot.ResumeBlock r = snap.getResume();
            StringBuilder sb = new StringBuilder("- Resume: ");
            if (r.getTitle() != null) sb.append("\"").append(r.getTitle()).append("\" ");
            if (r.getTargetJob() != null) sb.append("targeting ").append(r.getTargetJob());
            if (r.getDiagnosisScore() != null && r.getDiagnosisScore() > 0) {
                sb.append("; diagnosis score ").append(r.getDiagnosisScore()).append("/100");
            }
            if (r.getUpdatedAt() != null) sb.append(" (").append(r.getUpdatedAt().format(PROMPT_DATE_FMT)).append(")");
            lines.add(sb.toString());
        }

        if (snap.getInterview() != null) {
            UserProfileSnapshot.InterviewBlock i = snap.getInterview();
            StringBuilder sb = new StringBuilder("- Last interview: ");
            if (i.getPositionName() != null) sb.append(i.getPositionName());
            if (i.getDifficulty() != null) sb.append(" (").append(i.getDifficulty()).append(")");
            if (i.getLastScore() != null) sb.append(", score ").append(i.getLastScore()).append("/100");
            if (i.getStrongDimensions() != null && !i.getStrongDimensions().isEmpty()) {
                sb.append("; strengths: ").append(String.join(", ", i.getStrongDimensions()));
            }
            if (i.getWeakDimensions() != null && !i.getWeakDimensions().isEmpty()) {
                sb.append("; growth areas: ").append(String.join(", ", i.getWeakDimensions()));
            }
            if (i.getCompletedAt() != null) sb.append(" (").append(i.getCompletedAt().format(PROMPT_DATE_FMT)).append(")");
            lines.add(sb.toString());
        }

        if (snap.getPreferences() != null) {
            UserProfileSnapshot.PreferencesBlock p = snap.getPreferences();
            if (p.getTargetRole() != null) {
                lines.add("- Target role of interest: " + p.getTargetRole());
            }
        }

        return String.join("\n", lines);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mergeAssessment(Long userId, UserProfileSnapshot.AssessmentBlock block) {
        if (userId == null || block == null) return;
        UserProfileSnapshot current = read(userId);
        current.setAssessment(block);
        // Bubble suggested role up to preferences if the user doesn't have an
        // explicit one yet — that's how the assessment becomes a portal into
        // resume diagnosis / interview start without an extra UI step.
        if (block.getSuggestedRoles() != null && !block.getSuggestedRoles().isEmpty()) {
            UserProfileSnapshot.PreferencesBlock prefs = current.getPreferences();
            if (prefs == null) prefs = UserProfileSnapshot.PreferencesBlock.builder().build();
            if (prefs.getTargetRole() == null || prefs.getTargetRole().isBlank()) {
                prefs.setTargetRole(block.getSuggestedRoles().get(0));
                current.setPreferences(prefs);
            }
        }
        persist(userId, current);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mergeResume(Long userId, UserProfileSnapshot.ResumeBlock block) {
        if (userId == null || block == null) return;
        UserProfileSnapshot current = read(userId);
        current.setResume(block);
        persist(userId, current);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mergeInterview(Long userId, UserProfileSnapshot.InterviewBlock block) {
        if (userId == null || block == null) return;
        UserProfileSnapshot current = read(userId);
        current.setInterview(block);
        persist(userId, current);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void mergePreferences(Long userId, UserProfileSnapshot.PreferencesBlock block) {
        if (userId == null || block == null) return;
        UserProfileSnapshot current = read(userId);
        UserProfileSnapshot.PreferencesBlock existing = current.getPreferences();
        if (existing == null) {
            current.setPreferences(block);
        } else {
            // Field-by-field merge so we don't wipe earlier preferences
            // when only one field is being updated this time.
            if (block.getTargetRole() != null) existing.setTargetRole(block.getTargetRole());
            if (block.getInterviewMode() != null) existing.setInterviewMode(block.getInterviewMode());
            current.setPreferences(existing);
        }
        persist(userId, current);
    }

    /**
     * Best-effort persistence — snapshot writes are never on the user's
     * critical path (they happen after the real assessment / resume /
     * interview write committed), so we swallow failures and log them
     * loudly rather than failing the originating request.
     */
    private void persist(Long userId, UserProfileSnapshot snap) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("[snapshot] user {} not found, skipping merge", userId);
                return;
            }
            if (snap.getVersion() == null) snap.setVersion(1);
            snap.setUpdatedAt(LocalDateTime.now());
            user.setProfileSnapshot(objectMapper.writeValueAsString(snap));
            userRepository.save(user);
            log.debug("[snapshot] persisted for user {}", userId);
        } catch (Exception e) {
            log.warn("[snapshot] persist failed for user {}: {}", userId, e.toString());
        }
    }

    private UserProfileSnapshot parse(String json) {
        if (json == null || json.isBlank()) return UserProfileSnapshot.builder().build();
        try {
            UserProfileSnapshot s = objectMapper.readValue(json, UserProfileSnapshot.class);
            return s == null ? UserProfileSnapshot.builder().build() : s;
        } catch (Exception e) {
            log.warn("[snapshot] failed to parse profile_snapshot, returning empty: {}", e.toString());
            return UserProfileSnapshot.builder().build();
        }
    }

    private boolean isEmpty(UserProfileSnapshot s) {
        return s.getAssessment() == null
                && s.getResume() == null
                && s.getInterview() == null
                && s.getPreferences() == null;
    }
}
