package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.service.CareerService;
import com.group1.career.service.InterviewService;
import com.group1.career.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Homepage API", description = "Aggregated data feed for the homepage")
@RestController
@RequestMapping("/api/homepage")
@RequiredArgsConstructor
public class HomepageController {

    private final CareerService careerService;

    @Operation(summary = "Get homepage aggregated feed")
    @GetMapping("/feed")
    public Result<HomepageFeedDto> getHomepageFeed(@RequestParam(required = false) Long userId) {

        // 1. Career path recommendations
        List<CareerPath> paths = careerService.getAllPaths();
        List<CareerCardDto> careerCards = paths.stream()
                .limit(4)
                .map(p -> CareerCardDto.builder()
                        .pathId(p.getPathId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());

        // 2. Featured articles / news (mock data for now)
        List<ArticleDto> articles = Arrays.asList(
                ArticleDto.builder()
                        .title("2026 Top Tech Skills")
                        .summary("AI, Cloud, and Cyber Security remain the most sought-after skills...")
                        .imageUrl("/static/articles/tech-skills.jpg")
                        .build(),
                ArticleDto.builder()
                        .title("How to Ace Your First Interview")
                        .summary("Preparation is key. Here are 10 tips from senior HRs...")
                        .imageUrl("/static/articles/interview-tips.jpg")
                        .build(),
                ArticleDto.builder()
                        .title("Career Planning for Freshmen")
                        .summary("Starting early gives you a competitive advantage in the job market...")
                        .imageUrl("/static/articles/career-planning.jpg")
                        .build()
        );

        // 3. Quick stats
        QuickStatsDto stats = QuickStatsDto.builder()
                .totalCareerPaths(paths.size())
                .totalUsers(1280)   // In production: userService.count()
                .totalInterviews(356) // In production: interviewService.count()
                .build();

        HomepageFeedDto feed = HomepageFeedDto.builder()
                .careerCards(careerCards)
                .articles(articles)
                .stats(stats)
                .build();

        return Result.success(feed);
    }

    // ================= DTO Classes =================

    @Data @Builder @AllArgsConstructor
    public static class HomepageFeedDto {
        private List<CareerCardDto> careerCards;
        private List<ArticleDto> articles;
        private QuickStatsDto stats;
    }

    @Data @Builder @AllArgsConstructor
    public static class CareerCardDto {
        private Integer pathId;
        private String name;
        private String description;
    }

    @Data @Builder @AllArgsConstructor
    public static class ArticleDto {
        private String title;
        private String summary;
        private String imageUrl;
    }

    @Data @Builder @AllArgsConstructor
    public static class QuickStatsDto {
        private int totalCareerPaths;
        private int totalUsers;
        private int totalInterviews;
    }
}
