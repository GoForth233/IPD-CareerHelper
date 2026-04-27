package com.group1.career.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.*;
import com.group1.career.repository.*;
import com.group1.career.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentScaleRepository scaleRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentOptionRepository optionRepository;
    private final AssessmentRecordRepository recordRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<AssessmentScale> getAllScales() {
        return scaleRepository.findByIsActiveTrueOrderByScaleIdAsc();
    }

    @Override
    public List<AssessmentQuestion> getScaleQuestions(Long scaleId) {
        return questionRepository.findByScaleIdAndIsActiveTrueOrderBySortOrderAsc(scaleId);
    }

    @Override
    @Transactional
    public AssessmentRecord submitAndScore(Long userId, Long scaleId, Map<Long, Long> answers) {
        AssessmentScale scale = scaleRepository.findById(scaleId)
                .orElseThrow(() -> new RuntimeException("Scale not found: " + scaleId));

        List<Long> questionIds = new ArrayList<>(answers.keySet());
        List<Long> optionIds = new ArrayList<>(answers.values());
        List<AssessmentOption> selectedOptions = optionRepository.findByQuestionIdIn(questionIds);

        Map<Long, AssessmentOption> optionMap = selectedOptions.stream()
                .filter(o -> optionIds.contains(o.getOptionId()))
                .collect(Collectors.toMap(AssessmentOption::getOptionId, o -> o));

        Map<String, Integer> traitCounts = new HashMap<>();
        List<AssessmentAnswer> answerEntities = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : answers.entrySet()) {
            Long questionId = entry.getKey();
            Long optionId = entry.getValue();
            AssessmentOption option = optionMap.get(optionId);

            BigDecimal scoreSnapshot = BigDecimal.ZERO;
            if (option != null && option.getDimensionCode() != null) {
                traitCounts.merge(option.getDimensionCode(), 1, Integer::sum);
                scoreSnapshot = option.getScoreValue();
            }

            answerEntities.add(AssessmentAnswer.builder()
                    .questionId(questionId)
                    .optionId(optionId)
                    .scoreSnapshot(scoreSnapshot)
                    .build());
        }

        String portrait = generatePortrait(scale.getTitle(), traitCounts);

        String traitsJson = "{}";
        try {
            traitsJson = objectMapper.writeValueAsString(traitCounts);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize traits", e);
        }

        AssessmentRecord record = AssessmentRecord.builder()
                .userId(userId)
                .scaleId(scaleId)
                .status("COMPLETED")
                .resultSummary(portrait)
                .resultJson(traitsJson)
                .build();
        record = recordRepository.save(record);

        final Long recordId = record.getRecordId();
        answerEntities.forEach(a -> a.setRecordId(recordId));
        answerRepository.saveAll(answerEntities);

        return record;
    }

    @Override
    public List<AssessmentRecord> getUserRecords(Long userId) {
        return recordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public AssessmentRecord getRecord(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));
    }

    private String generatePortrait(String scaleTitle, Map<String, Integer> traits) {
        if (traits.isEmpty()) return "N/A";

        String title = scaleTitle.toUpperCase();
        if (title.contains("MBTI")) {
            StringBuilder sb = new StringBuilder();
            sb.append(get(traits, "E") >= get(traits, "I") ? "E" : "I");
            sb.append(get(traits, "S") >= get(traits, "N") ? "S" : "N");
            sb.append(get(traits, "T") >= get(traits, "F") ? "T" : "F");
            sb.append(get(traits, "J") >= get(traits, "P") ? "J" : "P");
            return sb.toString();
        } else {
            return traits.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining());
        }
    }

    private int get(Map<String, Integer> map, String key) {
        return map.getOrDefault(key, 0);
    }
}
