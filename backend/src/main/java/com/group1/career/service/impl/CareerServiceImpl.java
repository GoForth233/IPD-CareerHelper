package com.group1.career.service.impl;

import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.CareerNode;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.UserCareerProgress;
import com.group1.career.repository.CareerNodeRepository;
import com.group1.career.repository.CareerPathRepository;
import com.group1.career.repository.UserCareerProgressRepository;
import com.group1.career.service.CareerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerServiceImpl implements CareerService {

    private final CareerPathRepository pathRepository;
    private final CareerNodeRepository nodeRepository;
    private final UserCareerProgressRepository progressRepository;

    @Override
    public List<CareerPath> getAllPaths() {
        return pathRepository.findAll();
    }

    @Override
    public CareerPath getPathById(Integer pathId) {
        return pathRepository.findById(pathId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));
    }

    @Override
    public List<CareerNode> getPathNodes(Integer pathId) {
        return nodeRepository.findByPathIdOrderByLevelAsc(pathId);
    }

    @Override
    public List<UserCareerProgress> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void unlockNode(Long userId, Long nodeId) {
        UserCareerProgress progress = progressRepository.findByUserIdAndNodeId(userId, nodeId)
                .orElse(UserCareerProgress.builder()
                        .userId(userId)
                        .nodeId(nodeId)
                        .status("UNLOCKED")
                        .build());

        if ("LOCKED".equals(progress.getStatus())) {
            progress.setStatus("UNLOCKED");
        }

        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public void completeNode(Long userId, Long nodeId) {
        UserCareerProgress progress = progressRepository.findByUserIdAndNodeId(userId, nodeId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));

        progress.setStatus("COMPLETED");
        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public void initializeDefaultPaths() {
        CareerPath javaPath = pathRepository.findByCode("java-backend")
                .orElse(CareerPath.builder().code("java-backend").build());
        javaPath.setName("Java Backend Engineer");
        javaPath.setDescription("Become an excellent Java backend developer, mastering core skills such as Spring Boot, microservices, and databases.");
        javaPath = pathRepository.save(javaPath);

        if (nodeRepository.findByPathIdOrderByLevelAsc(javaPath.getPathId()).isEmpty()) {
            CareerNode node1 = nodeRepository.save(CareerNode.builder()
                    .pathId(javaPath.getPathId()).name("Java Basics").level(1).parentId(0L).build());
            CareerNode node2 = nodeRepository.save(CareerNode.builder()
                    .pathId(javaPath.getPathId()).name("Spring Boot Intro").level(2).parentId(node1.getNodeId()).build());
            nodeRepository.save(CareerNode.builder()
                    .pathId(javaPath.getPathId()).name("Database Design").level(2).parentId(node1.getNodeId()).build());
            nodeRepository.save(CareerNode.builder()
                    .pathId(javaPath.getPathId()).name("Spring Cloud Microservices").level(3).parentId(node2.getNodeId()).build());
        }

        CareerPath frontendPath = pathRepository.findByCode("frontend-engineer")
                .orElse(CareerPath.builder().code("frontend-engineer").build());
        frontendPath.setName("Frontend Engineer");
        frontendPath.setDescription("Become a modern frontend developer, mastering tech stacks like Vue, React, and TypeScript.");
        frontendPath = pathRepository.save(frontendPath);

        if (nodeRepository.findByPathIdOrderByLevelAsc(frontendPath.getPathId()).isEmpty()) {
            CareerNode fe1 = nodeRepository.save(CareerNode.builder()
                    .pathId(frontendPath.getPathId()).name("HTML/CSS Basics").level(1).parentId(0L).build());
            CareerNode fe2 = nodeRepository.save(CareerNode.builder()
                    .pathId(frontendPath.getPathId()).name("JavaScript Core").level(2).parentId(fe1.getNodeId()).build());
            nodeRepository.save(CareerNode.builder()
                    .pathId(frontendPath.getPathId()).name("Vue.js Framework").level(3).parentId(fe2.getNodeId()).build());
        }

        log.info("Initialized/Updated career paths: Java Backend, Frontend Engineer");
    }
}
