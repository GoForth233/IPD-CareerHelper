package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.AgentEvent;
import com.group1.career.model.entity.AgentUserProfile;
import com.group1.career.repository.AgentEventRepository;
import com.group1.career.repository.AgentUserProfileRepository;
import com.group1.career.service.AgentEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentEventServiceImpl implements AgentEventService {

    private final AgentEventRepository eventRepository;
    private final AgentUserProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    /**
     * Persists a single event. Runs in its own transaction (REQUIRES_NEW) so it
     * can be called safely from inside a read-only outer transaction.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String eventType, String source, Object payload) {
        try {
            String json = payload != null ? objectMapper.writeValueAsString(payload) : null;
            eventRepository.save(AgentEvent.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .source(source)
                    .eventPayload(json)
                    .build());
        } catch (Exception e) {
            log.warn("[agent-event] failed to record event type={} user={}: {}", eventType, userId, e.getMessage());
        }
    }

    /**
     * Detects a change in the primary risk code by comparing against the value
     * cached in {@code agent_user_profiles.primary_risk_code}. If changed:
     * <ol>
     *   <li>Writes a RISK_CHANGED event with before/after payload.</li>
     *   <li>Updates {@code agent_user_profiles.primary_risk_code} in place.</li>
     * </ol>
     * Also runs in REQUIRES_NEW so it is safe inside read-only callers.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRiskChange(Long userId, String newPrimaryCode, String newPrimaryLevel) {
        try {
            Optional<AgentUserProfile> profileOpt = profileRepository.findByUserId(userId);
            String previousCode = profileOpt.map(AgentUserProfile::getPrimaryRiskCode).orElse(null);

            if (newPrimaryCode != null && !newPrimaryCode.equals(previousCode)) {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("previousCode", previousCode);
                payload.put("newCode", newPrimaryCode);
                payload.put("newLevel", newPrimaryLevel);
                record(userId, "RISK_CHANGED", "SYSTEM", payload);

                profileOpt.ifPresent(profile -> {
                    profile.setPrimaryRiskCode(newPrimaryCode);
                    profileRepository.save(profile);
                });
                log.debug("[agent-event] RISK_CHANGED user={} {}→{}", userId, previousCode, newPrimaryCode);
            }
        } catch (Exception e) {
            log.warn("[agent-event] failed to record RISK_CHANGED user={}: {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentEvent> getRecentEvents(Long userId, int limit) {
        return eventRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, Math.max(1, Math.min(limit, 100))));
    }
}
