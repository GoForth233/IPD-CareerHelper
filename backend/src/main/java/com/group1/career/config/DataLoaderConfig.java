package com.group1.career.config;

import com.group1.career.model.entity.HomeArticle;
import com.group1.career.model.entity.HomeConsultation;
import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.repository.HomeArticleRepository;
import com.group1.career.repository.HomeConsultationRepository;
import com.group1.career.repository.InterviewQuestionRepository;
import com.group1.career.service.CareerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

/**
 * Data Loader Configuration.
 *
 * <p>Initialises default career paths and seeds the home page with a baseline
 * batch of articles + consultations so the feed is never visibly empty —
 * even when {@link com.group1.career.service.HomeContentRefreshJob}'s
 * Bilibili pull is offline. Disabled in the {@code test} profile.</p>
 */
@Slf4j
@Configuration
@Profile("!test")
public class DataLoaderConfig {

    @Bean
    public CommandLineRunner dataLoader(
            CareerService careerService,
            HomeArticleRepository articleRepository,
            HomeConsultationRepository consultationRepository,
            InterviewQuestionRepository questionRepository
    ) {
        return args -> {
            careerService.initializeDefaultPaths();
            seedHomeArticles(articleRepository);
            seedHomeConsultations(consultationRepository);
            seedInterviewQuestions(questionRepository);
        };
    }

    private void seedHomeArticles(HomeArticleRepository repo) {
        if (repo.count() > 0) return;
        log.info("[seed] populating home_articles with starter batch");
        LocalDateTime now = LocalDateTime.now();
        repo.save(HomeArticle.builder()
                .title("2026 春招技术岗最值钱的 5 项技能")
                .summary("AI 工程化、云原生、可观测性、数据建模和系统设计 — 看清趋势，少走弯路。")
                .imageUrl("/static/articles/tech-skills.jpg")
                .sourceUrl("/pages/map/index")
                .category("skill")
                .publishedAt(now.minusHours(3))
                .build());
        repo.save(HomeArticle.builder()
                .title("第一次面试就被问到「你最大的缺点」?用 STAR 模型回答")
                .summary("一份 HR 觉得「真诚而不暴露短板」的回答模板，附 5 个真实改写示例。")
                .imageUrl("/static/articles/interview-tips.jpg")
                .sourceUrl("/pages/interview/start")
                .category("interview")
                .publishedAt(now.minusDays(1))
                .build());
        repo.save(HomeArticle.builder()
                .title("校招简历的 3 个致命伤，AI 诊断告诉你怎么改")
                .summary("最常见的「项目经历堆砌」「岗位错配」「关键词缺失」全部上传 AI 一键扫描。")
                .imageUrl("/static/articles/career-planning.jpg")
                .sourceUrl("/pages/resume-ai/index")
                .category("resume")
                .publishedAt(now.minusDays(2))
                .build());
        repo.save(HomeArticle.builder()
                .title("Java 后端 vs 前端 vs 数据 — 选择困难症的 5 题决策清单")
                .summary("先做 MBTI/Holland 测评，再看对应岗位的真实工作日常，10 分钟搞定方向选择。")
                .imageUrl("/static/articles/tech-skills.jpg")
                .sourceUrl("/pages/assessment/index")
                .category("planning")
                .publishedAt(now.minusDays(3))
                .build());
        repo.save(HomeArticle.builder()
                .title("从应届到 Senior 的 5 个隐形阶段")
                .summary("把职业地图当作主线任务：每个节点对应的能力、项目和面试话术全部展开。")
                .imageUrl("/static/articles/career-planning.jpg")
                .sourceUrl("/pages/map/index")
                .category("planning")
                .publishedAt(now.minusDays(4))
                .build());
    }

    private void seedHomeConsultations(HomeConsultationRepository repo) {
        if (repo.count() > 0) return;
        log.info("[seed] populating home_consultations with starter batch");
        LocalDateTime now = LocalDateTime.now();
        repo.save(HomeConsultation.builder()
                .title("HR 视角：群面里被淘汰的人都做错了什么")
                .bodyMd("- 抢着发言但没有结构\n- 反驳队友不留台阶\n- 答完忘记 hand-off")
                .author("李学长 · 一线大厂 HR")
                .imageUrl("/static/articles/interview-tips.jpg")
                .publishedAt(now.minusHours(6))
                .build());
        repo.save(HomeConsultation.builder()
                .title("我用 AI 模拟面试 30 次的真实变化")
                .bodyMd("从最初的紧张到能够稳定输出 STAR 结构，雷达图 6 项里 4 项突破 80。")
                .author("王同学 · 计算机大三")
                .imageUrl("/static/articles/career-planning.jpg")
                .publishedAt(now.minusDays(2))
                .build());
        repo.save(HomeConsultation.builder()
                .title("简历上写「精通」前请回答这 3 个问题")
                .bodyMd("能口头讲清楚原理；能写出 demo；能反问面试官同样的题 — 三选三才算精通。")
                .author("吴老师 · 互联网架构师")
                .imageUrl("/static/articles/tech-skills.jpg")
                .publishedAt(now.minusDays(4))
                .build());
    }

    /**
     * Phase 4 — make sure the question market has at least a starter batch
     * before the first user contributes. The QBS draw also relies on at
     * least one matching row per (position, difficulty) so we cover a few
     * popular angles.
     */
    private void seedInterviewQuestions(InterviewQuestionRepository repo) {
        if (repo.count() > 0) return;
        log.info("[seed] populating interview_questions with starter batch");
        repo.save(InterviewQuestion.builder()
                .position("Java Developer").difficulty("Normal")
                .content("Walk me through a production memory leak you've debugged. What was the root cause and how did you confirm it?")
                .summary("JVM troubleshooting under prod load")
                .likes(8).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("Java Developer").difficulty("Hard")
                .content("Design a rate limiter that survives a node restart and works across a multi-region deployment. Walk through the trade-offs.")
                .summary("Distributed rate limiting design")
                .likes(15).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("Frontend Engineer").difficulty("Normal")
                .content("How would you debounce a search input that hits a remote API, while still showing instant feedback for the local typing state?")
                .summary("Debounce + UX feedback split")
                .likes(11).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("Frontend Engineer").difficulty("Easy")
                .content("Explain the difference between flexbox and grid. Give an example where one is clearly the right choice.")
                .summary("CSS layout fundamentals")
                .likes(6).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("Product Manager").difficulty("Normal")
                .content("Describe a feature you launched that under-performed. What did you learn from the post-mortem and how did you adjust the next launch?")
                .summary("Post-launch retrospective")
                .likes(9).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("Data Analyst").difficulty("Normal")
                .content("A dashboard shows DAU dropped 20% week-over-week. What's your investigation plan in the first 30 minutes?")
                .summary("Triage a sudden metric drop")
                .likes(13).drawCount(0).status("APPROVED").build());
        repo.save(InterviewQuestion.builder()
                .position("General").difficulty("Normal")
                .content("Tell me about a time you disagreed with your manager on a technical or product decision. How did the conversation play out?")
                .summary("Conflict + communication")
                .likes(7).drawCount(0).status("APPROVED").build());
    }
}
