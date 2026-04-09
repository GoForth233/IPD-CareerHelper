package com.group1.career.controller;

import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.service.CareerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomepageController.class)
public class HomepageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareerService careerService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Returns full feed with career cards, articles, and stats")
    public void testGetHomepageFeed_Success() throws Exception {
        List<CareerPath> paths = Arrays.asList(
                CareerPath.builder().pathId(1).name("Java Backend Engineer").description("Master Java backend").build(),
                CareerPath.builder().pathId(2).name("Frontend Engineer").description("Master frontend tech").build()
        );
        when(careerService.getAllPaths()).thenReturn(paths);

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.careerCards").isArray())
                .andExpect(jsonPath("$.data.careerCards.length()").value(2))
                .andExpect(jsonPath("$.data.careerCards[0].name").value("Java Backend Engineer"))
                .andExpect(jsonPath("$.data.articles").isArray())
                .andExpect(jsonPath("$.data.articles.length()").value(3))
                .andExpect(jsonPath("$.data.stats.totalCareerPaths").value(2))
                .andExpect(jsonPath("$.data.stats.totalUsers").value(1280))
                .andExpect(jsonPath("$.data.stats.totalInterviews").value(356));
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Career cards capped at 4 when more paths exist")
    public void testGetHomepageFeed_CareerCardsCappedAtFour() throws Exception {
        List<CareerPath> paths = Arrays.asList(
                CareerPath.builder().pathId(1).name("Path 1").build(),
                CareerPath.builder().pathId(2).name("Path 2").build(),
                CareerPath.builder().pathId(3).name("Path 3").build(),
                CareerPath.builder().pathId(4).name("Path 4").build(),
                CareerPath.builder().pathId(5).name("Path 5").build()
        );
        when(careerService.getAllPaths()).thenReturn(paths);

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.careerCards.length()").value(4))
                .andExpect(jsonPath("$.data.stats.totalCareerPaths").value(5));
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Works with userId query param")
    public void testGetHomepageFeed_WithUserId() throws Exception {
        List<CareerPath> paths = Arrays.asList(
                CareerPath.builder().pathId(1).name("Java Backend Engineer").build()
        );
        when(careerService.getAllPaths()).thenReturn(paths);

        mockMvc.perform(get("/api/homepage/feed").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.careerCards.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Always returns 3 articles")
    public void testGetHomepageFeed_AlwaysReturnsThreeArticles() throws Exception {
        when(careerService.getAllPaths()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.articles.length()").value(3))
                .andExpect(jsonPath("$.data.articles[0].title").value("2026 Top Tech Skills"))
                .andExpect(jsonPath("$.data.articles[1].title").value("How to Ace Your First Interview"))
                .andExpect(jsonPath("$.data.articles[2].title").value("Career Planning for Freshmen"));
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Stats reflect actual path count")
    public void testGetHomepageFeed_StatsReflectPathCount() throws Exception {
        when(careerService.getAllPaths()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stats.totalCareerPaths").value(0))
                .andExpect(jsonPath("$.data.careerCards.length()").value(0));
    }
}
