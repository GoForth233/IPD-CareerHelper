package com.group1.career.job;

import com.group1.career.model.NotificationTypes;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.UsageEvent;
import com.group1.career.model.entity.User;
import com.group1.career.repository.CheckInRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.repository.NotificationRepository;
import com.group1.career.repository.UsageEventRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.AiService;
import com.group1.career.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * F14: AI Proactive Outreach Job.
 *
 * <p>Runs every day at 19:00 and evaluates trigger rules for each active user.
 * At most {@value #DAILY_GLOBAL_LIMIT} users receive an AI-generated proactive
 * notification per day; each user may receive at most {@value #WEEKLY_PER_USER_LIMIT}
 * such messages per 7-day window.</p>
 *
 * <p>Trigger priority (only the highest-priority matching rule fires per user per day):
 * <ol>
 *   <li>Consecutive 3+ days without a check-in (caring reminder)</li>
 *   <li>Latest completed interview score &lt; 60 (improvement suggestion)</li>
 *   <li>Sunday — weekly focus preview (regardless of other triggers)</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiProactiveJob {

    private static final int DAILY_GLOBAL_LIMIT  = 20;
    private static final int WEEKLY_PER_USER_LIMIT = 3;

    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final InterviewRepository interviewRepository;
    private final NotificationRepository notificationRepository;
    private final AiService aiService;
    private final NotificationService notificationService;
    private final UsageEventRepository usageEventRepository;

    @Scheduled(cron = "0 0 19 * * ?")
    public void run() {
        log.info("[F14] AiProactiveJob starting — daily global limit={}", DAILY_GLOBAL_LIMIT);
        LocalDate today = LocalDate.now();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        int globalCount = 0;

        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null
                          && (u.getStatus() == null || u.getStatus() == 1))
                .toList();

        for (User user : candidates) {
            if (globalCount >= DAILY_GLOBAL_LIMIT) break;

            long weeklyCount = notificationRepository.countByUserIdAndTypeAndCreatedAtAfter(
                    user.getUserId(), NotificationTypes.AI_PROACTIVE, weekAgo);
            if (weeklyCount >= WEEKLY_PER_USER_LIMIT) continue;

            if (weeklyCount > 0) {
                boolean sentToday = notificationRepository
                        .countByUserIdAndTypeAndCreatedAtAfter(
                                user.getUserId(),
                                NotificationTypes.AI_PROACTIVE,
                                LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)) > 0;
                if (sentToday) continue;
            }

            TriggerResult trigger = determineTrigger(user.getUserId(), today);
            if (trigger == null) continue;

            try {
                String body = generateMessage(user, trigger);
                notificationService.push(user.getUserId(),
                        NotificationTypes.AI_PROACTIVE,
                        trigger.title(),
                        body,
                        trigger.link());

                usageEventRepository.save(UsageEvent.builder()
                        .userId(user.getUserId())
                        .eventType("AI_PROACTIVE_SENT")
                        .payload("{\"trigger\":\"" + trigger.type() + "\"}")
                        .build());

                globalCount++;
                log.info("[F14] proactive message sent to user={} trigger={}",
                        user.getUserId(), trigger.type());
            } catch (Exception e) {
                log.error("[F14] failed for user={}: {}", user.getUserId(), e.getMessage());
            }
        }

        log.info("[F14] AiProactiveJob done — sent={}", globalCount);
    }

    // ──────────────────────────────────────────────────────────────────

    private TriggerResult determineTrigger(Long userId, LocalDate today) {
        LocalDate threeDaysAgo = today.minusDays(3);
        List<LocalDate> recentCheckIns = checkInRepository.distinctDaysInRange(
                userId, threeDaysAgo, today.minusDays(1));
        if (recentCheckIns.isEmpty()) {
            return new TriggerResult(
                    "MISSED_CHECKIN",
                    "打卡提醒",
                    "你已经连续好几天没打卡了，今天来学习一下吧 💪",
                    "/pages/checkin/index");
        }

        List<Interview> interviews = interviewRepository.findByUserIdOrderByStartedAtDesc(userId);
        if (!interviews.isEmpty()) {
            Interview latest = interviews.get(0);
            if ("COMPLETED".equals(latest.getStatus())
                    && latest.getFinalScore() != null
                    && latest.getFinalScore() < 60) {
                return new TriggerResult(
                        "LOW_INTERVIEW_SCORE",
                        "面试复盘建议",
                        "你上次模拟面试还有提升空间，来看看哪里可以改进 🎯",
                        "/pages/interview/index");
            }
        }

        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new TriggerResult(
                    "WEEKLY_FOCUS",
                    "下周聚焦建议",
                    "新的一周即将开始，小职为你准备了本周学习聚焦建议 📅",
                    "/pages/map/index");
        }

        return null;
    }

    private String generateMessage(User user, TriggerResult trigger) {
        String nickname = user.getNickname() != null ? user.getNickname() : "同学";
        String prompt = switch (trigger.type()) {
            case "MISSED_CHECKIN" ->
                    "你是CareerLoop AI助手小职，请给用户「" + nickname +
                    "」写一条简短温馨的打卡提醒（1句话，不超过40字，轻松友好语气，不重复题目中内容）";
            case "LOW_INTERVIEW_SCORE" ->
                    "你是CareerLoop AI助手小职，用户「" + nickname +
                    "」最近模拟面试得分偏低，请写一条鼓励性的复盘提醒（1句话，不超过40字）";
            case "WEEKLY_FOCUS" ->
                    "你是CareerLoop AI助手小职，请给用户「" + nickname +
                    "」写一条周日下周学习聚焦提醒（1句话，不超过40字，积极向上）";
            default -> trigger.defaultMessage();
        };
        try {
            return aiService.chat(List.of(Map.of("role", "user", "content", prompt)), "qwen-turbo");
        } catch (Exception e) {
            log.warn("[F14] AI generation fallback for user={}: {}", user.getUserId(), e.getMessage());
            return trigger.defaultMessage();
        }
    }

    record TriggerResult(String type, String title, String defaultMessage, String link) {}
}
