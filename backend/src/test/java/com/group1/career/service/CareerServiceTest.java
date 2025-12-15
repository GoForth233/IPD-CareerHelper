package com.group1.career.service;

import com.group1.career.model.entity.CareerNode;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.UserCareerProgress;
import com.group1.career.repository.CareerNodeRepository;
import com.group1.career.repository.CareerPathRepository;
import com.group1.career.repository.UserCareerProgressRepository;
import com.group1.career.service.impl.CareerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareerServiceTest {

    @Mock
    private CareerPathRepository pathRepository;

    @Mock
    private CareerNodeRepository nodeRepository;

    @Mock
    private UserCareerProgressRepository progressRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CareerServiceImpl careerService;

    @Test
    @DisplayName("Test Get All Career Paths")
    public void testGetAllPaths_Success() {
        // Prepare test data
        CareerPath path1 = CareerPath.builder()
                .pathId(1)
                .code("java-backend")
                .name("Java Backend Engineer")
                .build();
        CareerPath path2 = CareerPath.builder()
                .pathId(2)
                .code("frontend-engineer")
                .name("Frontend Engineer")
                .build();

        when(pathRepository.findAll()).thenReturn(Arrays.asList(path1, path2));

        // Execute
        List<CareerPath> result = careerService.getAllPaths();

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Backend Engineer", result.get(0).getName());
        verify(pathRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get Path Nodes")
    public void testGetPathNodes_Success() {
        // Prepare
        Integer pathId = 1;
        CareerNode node1 = CareerNode.builder()
                .nodeId(1L)
                .pathId(pathId)
                .name("Java Basics")
                .level(1)
                .build();
        CareerNode node2 = CareerNode.builder()
                .nodeId(2L)
                .pathId(pathId)
                .name("Spring Boot")
                .level(2)
                .build();

        when(nodeRepository.findByPathIdOrderByLevelAsc(pathId))
                .thenReturn(Arrays.asList(node1, node2));

        // Execute
        List<CareerNode> result = careerService.getPathNodes(pathId);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getName());
        assertEquals(1, result.get(0).getLevel());
    }

    @Test
    @DisplayName("Test Get User Progress - Cache Hit")
    public void testGetUserProgress_CacheHit() {
        // Prepare
        Long userId = 1L;
        String cacheKey = "career:progress:" + userId;
        List<UserCareerProgress> cachedProgress = Arrays.asList(
                UserCareerProgress.builder().id(1L).userId(userId).nodeId(1L).status("COMPLETED").build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(cachedProgress);

        // Execute
        List<UserCareerProgress> result = careerService.getUserProgress(userId);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
        
        // Verify cache hit (no DB query)
        verify(progressRepository, never()).findByUserId(userId);
    }

    @Test
    @DisplayName("Test Get User Progress - Cache Miss")
    public void testGetUserProgress_CacheMiss() {
        // Prepare
        Long userId = 1L;
        String cacheKey = "career:progress:" + userId;
        List<UserCareerProgress> dbProgress = Arrays.asList(
                UserCareerProgress.builder().id(1L).userId(userId).nodeId(1L).status("UNLOCKED").build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(progressRepository.findByUserId(userId)).thenReturn(dbProgress);

        // Execute
        List<UserCareerProgress> result = careerService.getUserProgress(userId);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Verify cache miss flow: DB query + cache write
        verify(progressRepository, times(1)).findByUserId(userId);
        verify(valueOperations, times(1)).set(eq(cacheKey), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Test Unlock Node")
    public void testUnlockNode_Success() {
        // Prepare
        Long userId = 1L;
        Long nodeId = 10L;
        UserCareerProgress progress = UserCareerProgress.builder()
                .userId(userId)
                .nodeId(nodeId)
                .status("LOCKED")
                .build();

        when(progressRepository.findByUserIdAndNodeId(userId, nodeId))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any(UserCareerProgress.class)))
                .thenReturn(progress);

        // Execute
        careerService.unlockNode(userId, nodeId);

        // Verify
        verify(progressRepository, times(1)).save(argThat(p -> 
            "UNLOCKED".equals(p.getStatus())
        ));
        verify(redisTemplate, times(1)).delete("career:progress:" + userId);
    }

    @Test
    @DisplayName("Test Complete Node")
    public void testCompleteNode_Success() {
        // Prepare
        Long userId = 1L;
        Long nodeId = 10L;
        UserCareerProgress progress = UserCareerProgress.builder()
                .userId(userId)
                .nodeId(nodeId)
                .status("UNLOCKED")
                .build();

        when(progressRepository.findByUserIdAndNodeId(userId, nodeId))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any(UserCareerProgress.class)))
                .thenReturn(progress);

        // Execute
        careerService.completeNode(userId, nodeId);

        // Verify
        verify(progressRepository, times(1)).save(argThat(p -> 
            "COMPLETED".equals(p.getStatus())
        ));
        verify(redisTemplate, times(1)).delete("career:progress:" + userId);
    }
}

