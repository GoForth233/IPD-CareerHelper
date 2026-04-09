package com.group1.career.service.impl;

import com.group1.career.model.document.AssessmentResultDocument;
import com.group1.career.repository.AssessmentResultRepository;
import com.group1.career.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentResultRepository assessmentResultRepository;

    @Override
    public AssessmentResultDocument submitAndScore(Long userId, String assessmentType, Map<String, String> rawAnswers) {

        // Weight-based scoring: count occurrences of each trait option
        Map<String, Integer> calculatedTraits = new HashMap<>();

        if ("MBTI".equalsIgnoreCase(assessmentType)) {
            // MBTI scoring: E/I, S/N, T/F, J/P
            int eCount = 0, iCount = 0, sCount = 0, nCount = 0;
            int tCount = 0, fCount = 0, jCount = 0, pCount = 0;

            for (Map.Entry<String, String> entry : rawAnswers.entrySet()) {
                String answer = entry.getValue().toUpperCase();
                int qNum = Integer.parseInt(entry.getKey().replaceAll("[^0-9]", ""));
                int dimension = (qNum % 4);
                if (dimension == 1) { if ("A".equals(answer)) eCount++; else iCount++; }
                if (dimension == 2) { if ("A".equals(answer)) sCount++; else nCount++; }
                if (dimension == 3) { if ("A".equals(answer)) tCount++; else fCount++; }
                if (dimension == 0) { if ("A".equals(answer)) jCount++; else pCount++; }
            }

            calculatedTraits.put("E", eCount);
            calculatedTraits.put("I", iCount);
            calculatedTraits.put("S", sCount);
            calculatedTraits.put("N", nCount);
            calculatedTraits.put("T", tCount);
            calculatedTraits.put("F", fCount);
            calculatedTraits.put("J", jCount);
            calculatedTraits.put("P", pCount);
        } else {
            // Holland: R/I/A/S/E/C generic scoring
            for (Map.Entry<String, String> entry : rawAnswers.entrySet()) {
                String trait = entry.getValue();
                calculatedTraits.merge(trait, 1, Integer::sum);
            }
        }

        // Generate final portrait string (e.g., "INTJ" or "RIA")
        String portrait = generatePortrait(assessmentType, calculatedTraits);

        AssessmentResultDocument doc = AssessmentResultDocument.builder()
                .userId(userId)
                .assessmentType(assessmentType)
                .rawAnswers(rawAnswers)
                .calculatedTraits(calculatedTraits)
                .finalPortrait(portrait)
                .createdAt(new Date())
                .build();

        return assessmentResultRepository.save(doc);
    }

    @Override
    public List<AssessmentResultDocument> getUserResults(Long userId) {
        return assessmentResultRepository.findByUserId(userId);
    }

    private String generatePortrait(String type, Map<String, Integer> traits) {
        if ("MBTI".equalsIgnoreCase(type)) {
            StringBuilder sb = new StringBuilder();
            sb.append(traits.getOrDefault("E", 0) >= traits.getOrDefault("I", 0) ? "E" : "I");
            sb.append(traits.getOrDefault("S", 0) >= traits.getOrDefault("N", 0) ? "S" : "N");
            sb.append(traits.getOrDefault("T", 0) >= traits.getOrDefault("F", 0) ? "T" : "F");
            sb.append(traits.getOrDefault("J", 0) >= traits.getOrDefault("P", 0) ? "J" : "P");
            return sb.toString();
        } else {
            // Return top 3 Holland codes
            return traits.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining());
        }
    }
}
