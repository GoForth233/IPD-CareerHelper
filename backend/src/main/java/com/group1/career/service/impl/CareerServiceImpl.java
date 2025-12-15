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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerServiceImpl implements CareerService {

    private final CareerPathRepository pathRepository;
    private final CareerNodeRepository nodeRepository;
    private final UserCareerProgressRepository progressRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CAREER_PROGRESS_PREFIX = "career:progress:";

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
        String cacheKey = CAREER_PROGRESS_PREFIX + userId;

        // 1. Check Cache
        @SuppressWarnings("unchecked")
        List<UserCareerProgress> cachedProgress = (List<UserCareerProgress>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProgress != null) {
            log.info("Redis Cache Hit: {}", cacheKey);
            return cachedProgress;
        }

        // 2. Query DB
        List<UserCareerProgress> progress = progressRepository.findByUserId(userId);

        // 3. Write Cache (1 hour)
        redisTemplate.opsForValue().set(cacheKey, progress, 1, TimeUnit.HOURS);

        return progress;
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

        // Clear cache
        redisTemplate.delete(CAREER_PROGRESS_PREFIX + userId);
    }

    @Override
    @Transactional
    public void completeNode(Long userId, Long nodeId) {
        UserCareerProgress progress = progressRepository.findByUserIdAndNodeId(userId, nodeId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_ERROR));

        progress.setStatus("COMPLETED");
        progressRepository.save(progress);

        // Clear cache
        redisTemplate.delete(CAREER_PROGRESS_PREFIX + userId);
    }

    @Override
    @Transactional
    public void initializeDefaultPaths() {
        // Check if already initialized
        if (pathRepository.count() > 0) {
            log.info("Career paths already initialized");
            return;
        }

        // 创建 Java 后端工程师路径
        CareerPath javaPath = pathRepository.save(CareerPath.builder()
                .code("java-backend")
                .name("Java后端工程师")
                .description("成为一名优秀的Java后端开发工程师，掌握Spring Boot、微服务、数据库等核心技能")
                .build());

        // 创建节点
        CareerNode node1 = nodeRepository.save(CareerNode.builder()
                .pathId(javaPath.getPathId())
                .name("Java 基础")
                .level(1)
                .parentId(0L)
                .build());

        CareerNode node2 = nodeRepository.save(CareerNode.builder()
                .pathId(javaPath.getPathId())
                .name("Spring Boot 入门")
                .level(2)
                .parentId(node1.getNodeId())
                .build());

        CareerNode node3 = nodeRepository.save(CareerNode.builder()
                .pathId(javaPath.getPathId())
                .name("数据库设计")
                .level(2)
                .parentId(node1.getNodeId())
                .build());

        CareerNode node4 = nodeRepository.save(CareerNode.builder()
                .pathId(javaPath.getPathId())
                .name("Spring Cloud 微服务")
                .level(3)
                .parentId(node2.getNodeId())
                .build());

        // 创建前端工程师路径
        CareerPath frontendPath = pathRepository.save(CareerPath.builder()
                .code("frontend-engineer")
                .name("前端工程师")
                .description("成为一名现代化前端开发工程师，掌握Vue、React、TypeScript等前端技术栈")
                .build());

        CareerNode fe1 = nodeRepository.save(CareerNode.builder()
                .pathId(frontendPath.getPathId())
                .name("HTML/CSS 基础")
                .level(1)
                .parentId(0L)
                .build());

        CareerNode fe2 = nodeRepository.save(CareerNode.builder()
                .pathId(frontendPath.getPathId())
                .name("JavaScript 核心")
                .level(2)
                .parentId(fe1.getNodeId())
                .build());

        CareerNode fe3 = nodeRepository.save(CareerNode.builder()
                .pathId(frontendPath.getPathId())
                .name("Vue.js 框架")
                .level(3)
                .parentId(fe2.getNodeId())
                .build());

        log.info("Initialized career paths: Java Backend, Frontend Engineer");
    }
}

