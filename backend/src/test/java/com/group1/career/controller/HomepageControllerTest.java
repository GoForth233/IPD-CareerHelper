package com.group1.career.controller;

import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.HomeArticle;
import com.group1.career.model.entity.HomeConsultation;
import com.group1.career.model.entity.HomeVideo;
import com.group1.career.repository.HomeArticleRepository;
import com.group1.career.repository.HomeConsultationRepository;
import com.group1.career.repository.HomeVideoRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.CareerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private UserRepository userRepository;

    @MockitoBean
    private InterviewRepository interviewRepository;

    /** Phase 2 introduced three home-feed tables (videos / articles /
     *  consultations). The controller still works when these are empty,
     *  which matches what the seeded-but-unused profile in tests gives us. */
    @MockitoBean
    private HomeVideoRepository homeVideoRepository;

    @MockitoBean
    private HomeArticleRepository homeArticleRepository;

    @MockitoBean
    private HomeConsultationRepository homeConsultationRepository;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(userRepository.count()).thenReturn(1280L);
        when(interviewRepository.count()).thenReturn(356L);
        // The home-feed repos default to empty lists; specific tests can
        // override when they want to assert content shape.
        when(homeVideoRepository.sampleByRand(anyLong(), anyInt())).thenReturn(Collections.emptyList());
        when(homeVideoRepository.findAllByOrderBySortScoreDesc(any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(homeArticleRepository.findAllByOrderByPublishedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(homeConsultationRepository.findAllByOrderByPublishedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Returns full feed with all sections")
    public void testGetHomepageFeed_Success() throws Exception {
        List<CareerPath> paths = Arrays.asList(
                CareerPath.builder().pathId(1).name("Java Backend Engineer").description("Master Java backend").build(),
                CareerPath.builder().pathId(2).name("Frontend Engineer").description("Master frontend tech").build()
        );
        when(careerService.getAllPaths()).thenReturn(paths);

        when(homeVideoRepository.sampleByRand(anyLong(), anyInt())).thenReturn(List.of(
                HomeVideo.builder().id(1L).bvid("BV1xx411c7mu").title("面试问题怎么答").coverUrl("https://x/y.jpg")
                        .upName("UP主").durationSec(720).viewCount(123456L).keyword("面试").build()
        ));
        when(homeArticleRepository.findAllByOrderByPublishedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(List.of(
                        HomeArticle.builder().id(1L).title("STAR 模型答题")
                                .summary("一份 HR 觉得「真诚而不暴露短板」的回答模板").imageUrl("/x").sourceUrl("/pages/x").category("interview").build()
                ));
        when(homeConsultationRepository.findAllByOrderByPublishedAtDescIdDesc(any(Pageable.class)))
                .thenReturn(List.of(
                        HomeConsultation.builder().id(1L).title("HR 视角").bodyMd("- 抢着发言但没有结构").author("李学长").build()
                ));

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.careerCards").isArray())
                .andExpect(jsonPath("$.data.careerCards.length()").value(2))
                .andExpect(jsonPath("$.data.careerCards[0].name").value("Java Backend Engineer"))
                .andExpect(jsonPath("$.data.videos").isArray())
                .andExpect(jsonPath("$.data.videos.length()").value(1))
                .andExpect(jsonPath("$.data.videos[0].bvid").value("BV1xx411c7mu"))
                .andExpect(jsonPath("$.data.videos[0].url").value("https://www.bilibili.com/video/BV1xx411c7mu"))
                .andExpect(jsonPath("$.data.articles").isArray())
                .andExpect(jsonPath("$.data.articles.length()").value(1))
                .andExpect(jsonPath("$.data.consultations").isArray())
                .andExpect(jsonPath("$.data.consultations.length()").value(1))
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
        when(careerService.getAllPaths()).thenReturn(List.of(
                CareerPath.builder().pathId(1).name("Java Backend Engineer").build()
        ));

        mockMvc.perform(get("/api/homepage/feed").param("userId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.careerCards.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/homepage/feed - Empty when no content seeded")
    public void testGetHomepageFeed_EmptyState() throws Exception {
        when(careerService.getAllPaths()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/homepage/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.videos.length()").value(0))
                .andExpect(jsonPath("$.data.articles.length()").value(0))
                .andExpect(jsonPath("$.data.consultations.length()").value(0))
                .andExpect(jsonPath("$.data.careerCards.length()").value(0));
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
