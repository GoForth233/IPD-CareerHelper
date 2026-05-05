package com.group1.career.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.dto.CareerAgentPlanDto;
import com.group1.career.model.dto.CareerAgentTodayDto;
import com.group1.career.model.dto.CareerAgentRiskDto;
import com.group1.career.model.dto.UserProfileSnapshot;
import com.group1.career.model.entity.AgentTask;
import com.group1.career.model.entity.User;
import com.group1.career.model.entity.UserCareerPlan;
import com.group1.career.repository.AgentTaskRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.AgentEventService;
import com.group1.career.service.AgentStateService;
import com.group1.career.service.CareerPlanService;
import com.group1.career.service.CareerAgentService;
import com.group1.career.service.CheckInService;
import com.group1.career.service.UserProfileSnapshotService;
import com.group1.career.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CareerAgentServiceImpl implements CareerAgentService {

    private final UserRepository userRepository;
    private final UserProfileSnapshotService snapshotService;
    private final CheckInService checkInService;
    private final AgentTaskRepository taskRepository;
    private final CareerPlanService careerPlanService;
    private final AgentEventService agentEventService;
    private final AgentStateService agentStateService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CareerAgentTodayDto getToday(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        UserProfileSnapshot snapshot = snapshotService.read(userId);
        CheckInService.CheckInStatus checkIn = checkInService.getStatus(userId);

        UserProfileSnapshot.AssessmentBlock assessment = snapshot.getAssessment();
        UserProfileSnapshot.ResumeBlock resume = snapshot.getResume();
        UserProfileSnapshot.InterviewBlock interview = snapshot.getInterview();
        UserProfileSnapshot.PreferencesBlock preferences = snapshot.getPreferences();

        String targetRole = firstText(
                preferences != null ? preferences.getTargetRole() : null,
                resume != null ? resume.getTargetJob() : null,
                interview != null ? interview.getPositionName() : null
        );
        int progress = progress(assessment, resume, interview, targetRole, checkIn);
        List<String> risks = new ArrayList<>();
        List<CareerAgentTodayDto.Action> actions = new ArrayList<>();

        String stage;
        String headline;
        String focus;
        String reason;

        if (!hasText(targetRole) && assessment == null) {
            stage = "DIRECTION_DISCOVERY";
            headline = "Start by finding a career direction";
            focus = "Complete one assessment and choose a target role";
            reason = "I do not have enough structured career direction data yet, so the highest-value step is to build your baseline profile.";
            risks.add("Target role is not set yet");
            risks.add("No assessment result is available for role matching");
            actions.add(action("Take assessment", "/pages/assessment/index", "ASSESSMENT", "HIGH"));
            actions.add(action("Ask AI to compare roles", "/pages/assistant/index", "CHAT", "MEDIUM"));
        } else if (!hasText(targetRole)) {
            stage = "TARGET_ROLE_SELECTION";
            headline = "Choose a concrete target role";
            focus = "Turn your assessment result into one primary job target";
            reason = "You already have some self-knowledge, but the agent needs a clear target role before it can push resume and interview preparation effectively.";
            risks.add("Career direction is still too broad");
            actions.add(action("Open assessment results", "/pages/assessment/index", "ASSESSMENT", "HIGH"));
            actions.add(action("Discuss target role with AI", "/pages/assistant/index", "CHAT", "HIGH"));
        } else if (resume == null) {
            stage = "RESUME_BOOTSTRAP";
            headline = "Build a resume for " + targetRole;
            focus = "Create or upload your first resume draft";
            reason = "Your target direction is visible, but I cannot evaluate application readiness until a resume exists.";
            risks.add("No resume data is available for the target role");
            actions.add(action("Create AI resume", "/pages/resume-ai/index", "RESUME", "HIGH"));
            actions.add(action("Open resume module", "/pages/resume/index", "RESUME", "MEDIUM"));
        } else if (resume.getDiagnosisScore() != null && resume.getDiagnosisScore() < 70) {
            stage = "RESUME_IMPROVEMENT";
            headline = "Improve your resume before applying";
            focus = "Raise the resume diagnosis score above 70";
            reason = "Your target role is clear, but the current resume score suggests the application material may hold you back.";
            risks.add("Resume diagnosis score is below the recommended threshold");
            actions.add(action("Optimize resume", "/pages/resume-ai/index", "RESUME", "HIGH"));
            actions.add(action("Ask strict agent to review resume", "/pages/assistant/index", "CHAT", "MEDIUM"));
        } else if (interview == null) {
            stage = "INTERVIEW_BOOTSTRAP";
            headline = "Start interview practice for " + targetRole;
            focus = "Complete one mock interview this week";
            reason = "Your direction and resume have enough signal. The next risk is whether you can explain your experience under interview pressure.";
            risks.add("No completed interview practice is available yet");
            actions.add(action("Start mock interview", "/pages/interview/start", "INTERVIEW", "HIGH"));
            actions.add(action("Practice in assistant", "/pages/assistant/index", "CHAT", "MEDIUM"));
        } else if (interview.getLastScore() != null && interview.getLastScore() < 70) {
            stage = "INTERVIEW_IMPROVEMENT";
            headline = "Fix weak interview dimensions";
            focus = "Practice the weakest interview dimension once";
            reason = "Your interview record shows a score below the readiness threshold, so today should focus on targeted practice rather than broad learning.";
            risks.add("Last interview score is below the recommended threshold");
            if (interview.getWeakDimensions() != null && !interview.getWeakDimensions().isEmpty()) {
                risks.add("Weak dimensions: " + String.join(", ", interview.getWeakDimensions()));
            }
            actions.add(action("Practice interview", "/pages/interview/start", "INTERVIEW", "HIGH"));
            actions.add(action("Ask mock interviewer", "/pages/assistant/index", "CHAT", "MEDIUM"));
        } else if (checkIn.getWeeklyDays() < 3) {
            stage = "EXECUTION_RHYTHM";
            headline = "Rebuild your weekly execution rhythm";
            focus = "Finish one core career action today";
            reason = "Your profile looks usable, but recent check-in activity is low. The agent should now push consistency, not more planning.";
            risks.add("Weekly check-in activity is below 3 days");
            actions.add(action("View check-in plan", "/pages/checkin/index", "CHECKIN", "HIGH"));
            actions.add(action("Pick today's AI task", "/pages/assistant/index", "CHAT", "MEDIUM"));
        } else {
            stage = "CAREER_MOMENTUM";
            headline = "Keep momentum toward " + targetRole;
            focus = "Do one focused improvement task and keep your streak";
            reason = "Your direction, resume, and interview signals are available. The best next step is continuous improvement and tracking.";
            actions.add(action("Review career plan", "/pages/assistant/index", "CHAT", "MEDIUM"));
            actions.add(action("Open skill map", "/pages/map/index", "LEARNING", "MEDIUM"));
        }

        if (checkIn.getTodayCompleted() < checkIn.getTodayTotal()) {
            risks.add("Today's core progress is not complete yet");
        }
        Optional<UserCareerPlan> currentPlan = careerPlanService.getCurrent(userId);
        List<String> weeklyFocus = currentPlan.map(plan -> weeklyFocusItems(plan).stream().limit(2).toList()).orElseGet(List::of);
        if (!weeklyFocus.isEmpty()) {
            risks.add("Today's work is aligned with the current long-term plan");
            reason = reason + " I also found your long-term plan, so today's tasks include weekly focus items from that plan.";
            for (String item : weeklyFocus) {
                actions.add(planAction(item));
            }
        }
        syncTodayTasks(userId, focus, actions);
        agentStateService.refresh(userId, stage, null, targetRole, null);

        return CareerAgentTodayDto.builder()
                .stage(stage)
                .riskLevel(riskLevel(risks.size()))
                .headline(headline)
                .reason(personalize(reason, user))
                .todayFocus(focus)
                .progressPercent(progress)
                .riskReasons(risks)
                .actions(actions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CareerAgentRiskDto getRiskWatch(Long userId) {
        UserProfileSnapshot snapshot = snapshotService.read(userId);
        CheckInService.CheckInStatus checkIn = checkInService.getStatus(userId);
        LocalDate today = LocalDate.now();
        List<AgentTask> recentTasks = taskRepository.findByUserIdAndDueDateBetweenOrderByDueDateDescCreatedAtDesc(
                userId, today.minusDays(6), today);

        UserProfileSnapshot.AssessmentBlock assessment = snapshot.getAssessment();
        UserProfileSnapshot.ResumeBlock resume = snapshot.getResume();
        UserProfileSnapshot.InterviewBlock interview = snapshot.getInterview();
        UserProfileSnapshot.PreferencesBlock preferences = snapshot.getPreferences();
        String targetRole = firstText(
                preferences != null ? preferences.getTargetRole() : null,
                resume != null ? resume.getTargetJob() : null,
                interview != null ? interview.getPositionName() : null
        );

        List<CareerAgentRiskDto.RiskItem> risks = new ArrayList<>();
        risks.add(directionRisk(assessment, targetRole));
        risks.add(resumeRisk(resume, targetRole));
        risks.add(interviewRisk(interview));
        risks.add(executionRisk(checkIn, recentTasks));
        risks.add(profileRisk(assessment, resume, interview, targetRole));
        risks.sort(Comparator.comparing(CareerAgentRiskDto.RiskItem::getScore).reversed());

        CareerAgentRiskDto.RiskItem primary = risks.get(0);
        agentEventService.recordRiskChange(userId, primary.getCode(), primary.getLevel());
        return CareerAgentRiskDto.builder()
                .overallLevel(primary.getLevel())
                .primaryRiskCode(primary.getCode())
                .primaryRiskTitle(primary.getTitle())
                .summary(primary.getReason())
                .risks(risks)
                .build();
    }

    @Override
    public CareerAgentPlanDto getPlanSummary(Long userId) {
        Optional<UserCareerPlan> plan = careerPlanService.getCurrent(userId);
        return plan.map(this::toPlanDto).orElseGet(() -> CareerAgentPlanDto.builder()
                .hasPlan(false)
                .planHealth("MISSING")
                .adjustmentReason("No long-term plan exists yet.")
                .weeklyFocus(List.of())
                .build());
    }

    @Override
    public CareerAgentPlanDto ensurePlan(Long userId) {
        Optional<UserCareerPlan> existing = careerPlanService.getCurrent(userId);
        if (existing.isPresent()) {
            return toPlanDto(existing.get());
        }
        UserProfileSnapshot snapshot = snapshotService.read(userId);
        String targetRole = inferTargetRole(snapshot);
        return toPlanDto(careerPlanService.generate(userId, targetRole));
    }

    @Override
    @Transactional
    public List<AgentTask> getTodayTasks(Long userId) {
        getToday(userId);
        return taskRepository.findByUserIdAndDueDateOrderByCreatedAtDesc(userId, LocalDate.now());
    }

    @Override
    public List<AgentTask> getOpenTasks(Long userId) {
        return taskRepository.findByUserIdAndStatusOrderByDueDateAscCreatedAtDesc(userId, "TODO");
    }

    @Override
    @Transactional
    public AgentTask completeTask(Long userId, Long taskId) {
        AgentTask task = ownedTask(userId, taskId);
        task.setStatus("DONE");
        task.setCompletedAt(LocalDateTime.now());
        AgentTask saved = taskRepository.save(task);
        agentEventService.record(userId, "TASK_COMPLETED", "USER",
                Map.of("taskId", saved.getTaskId(), "taskKey", saved.getTaskKey(),
                        "title", saved.getTitle(), "taskType", saved.getTaskType()));
        return saved;
    }

    @Override
    @Transactional
    public AgentTask dismissTask(Long userId, Long taskId) {
        AgentTask task = ownedTask(userId, taskId);
        task.setStatus("DISMISSED");
        task.setCompletedAt(null);
        AgentTask saved = taskRepository.save(task);
        agentEventService.record(userId, "TASK_DISMISSED", "USER",
                Map.of("taskId", saved.getTaskId(), "taskKey", saved.getTaskKey(),
                        "title", saved.getTitle(), "taskType", saved.getTaskType()));
        return saved;
    }

    private void syncTodayTasks(Long userId, String focus, List<CareerAgentTodayDto.Action> actions) {
        LocalDate today = LocalDate.now();
        for (CareerAgentTodayDto.Action action : actions) {
            String taskKey = taskKey(action);
            taskRepository.findByUserIdAndDueDateAndTaskKey(userId, today, taskKey)
                    .orElseGet(() -> taskRepository.save(AgentTask.builder()
                            .userId(userId)
                            .taskKey(taskKey)
                            .title(action.getLabel())
                            .description(taskDescription(focus, action))
                            .taskType(action.getType())
                            .priority(action.getPriority())
                            .status("TODO")
                            .target(action.getTarget())
                            .source(hasText(action.getSource()) ? action.getSource() : "DAILY_AGENT")
                            .dueDate(today)
                            .build()));
        }
    }

    private AgentTask ownedTask(Long userId, Long taskId) {
        AgentTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BizException("Agent task not found"));
        if (!userId.equals(task.getUserId())) {
            throw new BizException("Agent task not found");
        }
        return task;
    }

    private String taskKey(CareerAgentTodayDto.Action action) {
        String source = hasText(action.getSource()) ? action.getSource() : "DAILY_AGENT";
        return (source + ":" + action.getType() + ":" + action.getLabel())
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String taskDescription(String focus, CareerAgentTodayDto.Action action) {
        if ("PLAN_WEEKLY".equals(action.getSource())) {
            return "Weekly focus from your long-term career plan.";
        }
        return focus;
    }

    private CareerAgentTodayDto.Action planAction(String weeklyFocus) {
        return CareerAgentTodayDto.Action.builder()
                .label(shortTaskLabel(weeklyFocus))
                .target("/pages/map/index?tab=plan")
                .type("PLAN")
                .priority("HIGH")
                .source("PLAN_WEEKLY")
                .build();
    }

    private String shortTaskLabel(String text) {
        String value = text == null ? "" : text.trim();
        if (value.length() <= 24) return value;
        return value.substring(0, 24) + "...";
    }

    private List<String> weeklyFocusItems(UserCareerPlan plan) {
        JsonNode weeklyFocusJson = readJson(plan.getWeeklyFocusJson());
        List<String> weeklyFocus = new ArrayList<>();
        if (weeklyFocusJson.isArray()) {
            for (JsonNode item : weeklyFocusJson) {
                if (!item.asText("").isBlank()) weeklyFocus.add(item.asText());
            }
        }
        return weeklyFocus;
    }

    private CareerAgentPlanDto toPlanDto(UserCareerPlan plan) {
        JsonNode milestones = readJson(plan.getMilestonesJson());
        List<String> weeklyFocus = weeklyFocusItems(plan);
        JsonNode firstMilestone = milestones.isArray() && !milestones.isEmpty() ? milestones.get(0) : null;
        return CareerAgentPlanDto.builder()
                .hasPlan(true)
                .targetRole(plan.getTargetRole())
                .planHealth(planHealth(plan, weeklyFocus))
                .adjustmentReason(planAdjustmentReason(plan, weeklyFocus))
                .nextMilestoneHorizon(firstMilestone != null ? firstMilestone.path("horizon").asText("") : "")
                .nextMilestoneTitle(firstMilestone != null ? firstMilestone.path("title").asText("") : "")
                .weeklyFocus(weeklyFocus)
                .generatedAt(plan.getGeneratedAt() != null ? plan.getGeneratedAt().toString() : null)
                .lastUpdatedAt(plan.getLastUpdatedAt() != null ? plan.getLastUpdatedAt().toString() : null)
                .version(plan.getVersion())
                .build();
    }

    private JsonNode readJson(String raw) {
        try {
            if (raw == null || raw.isBlank()) return objectMapper.createArrayNode();
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            return objectMapper.createArrayNode();
        }
    }

    private String planHealth(UserCareerPlan plan, List<String> weeklyFocus) {
        if (plan.getLastUpdatedAt() == null) return "NEEDS_REFRESH";
        if (plan.getLastUpdatedAt().isBefore(LocalDateTime.now().minusDays(14))) return "NEEDS_REFRESH";
        if (weeklyFocus.isEmpty()) return "NEEDS_REFRESH";
        return "ON_TRACK";
    }

    private String planAdjustmentReason(UserCareerPlan plan, List<String> weeklyFocus) {
        if (plan.getLastUpdatedAt() == null) return "The plan has no update timestamp.";
        if (plan.getLastUpdatedAt().isBefore(LocalDateTime.now().minusDays(14))) return "The plan is older than 14 days and may need adjustment.";
        if (weeklyFocus.isEmpty()) return "Weekly focus is missing, so today's tasks cannot align with a long-term path.";
        return "Long-term plan is available and today's actions can stay aligned with it.";
    }

    private String inferTargetRole(UserProfileSnapshot snapshot) {
        UserProfileSnapshot.ResumeBlock resume = snapshot.getResume();
        UserProfileSnapshot.InterviewBlock interview = snapshot.getInterview();
        UserProfileSnapshot.PreferencesBlock preferences = snapshot.getPreferences();
        return firstText(
                preferences != null ? preferences.getTargetRole() : null,
                resume != null ? resume.getTargetJob() : null,
                interview != null ? interview.getPositionName() : null,
                "互联网行业职位"
        );
    }

    private CareerAgentRiskDto.RiskItem directionRisk(UserProfileSnapshot.AssessmentBlock assessment, String targetRole) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        if (!hasText(targetRole)) {
            score += 45;
            reasons.add("target role is not selected");
        }
        if (assessment == null) {
            score += 35;
            reasons.add("assessment baseline is missing");
        }
        String reason = reasons.isEmpty()
                ? "Career direction has enough baseline signal for now."
                : "Direction risk exists because " + String.join(" and ", reasons) + ".";
        return risk("DIRECTION_RISK", "Direction clarity", score, score >= 60 ? "RISING" : "STABLE",
                reason, hasText(targetRole) ? "Keep the target role visible while improving execution." : "Complete assessment and choose one primary role.");
    }

    private CareerAgentRiskDto.RiskItem resumeRisk(UserProfileSnapshot.ResumeBlock resume, String targetRole) {
        int score = 0;
        String reason = "Resume readiness is acceptable for the current stage.";
        String recommendation = "Keep the resume aligned with your target role.";
        if (hasText(targetRole) && resume == null) {
            score = 70;
            reason = "You have a target direction but no resume signal for the agent to evaluate.";
            recommendation = "Create or upload a resume for " + targetRole + ".";
        } else if (resume != null && resume.getDiagnosisScore() != null && resume.getDiagnosisScore() < 70) {
            score = 65;
            reason = "Resume diagnosis score is below 70, so application material may block interviews.";
            recommendation = "Optimize projects and keywords before increasing applications.";
        } else if (resume == null) {
            score = 35;
            reason = "Resume data is missing, but target direction is not clear yet.";
            recommendation = "Set target role first, then create a resume draft.";
        }
        return risk("RESUME_RISK", "Resume readiness", score, score >= 65 ? "RISING" : "STABLE", reason, recommendation);
    }

    private CareerAgentRiskDto.RiskItem interviewRisk(UserProfileSnapshot.InterviewBlock interview) {
        int score = 25;
        String reason = "Interview preparation has no urgent negative signal.";
        String recommendation = "Keep regular practice after resume readiness improves.";
        if (interview == null) {
            score = 55;
            reason = "No completed mock interview is available, so interview readiness is unknown.";
            recommendation = "Complete one mock interview to expose weak dimensions.";
        } else if (interview.getLastScore() != null && interview.getLastScore() < 70) {
            score = 70;
            reason = "Last interview score is below 70.";
            if (interview.getWeakDimensions() != null && !interview.getWeakDimensions().isEmpty()) {
                reason += " Weak dimensions: " + String.join(", ", interview.getWeakDimensions()) + ".";
            }
            recommendation = "Run targeted practice for the weakest dimension.";
        }
        return risk("INTERVIEW_RISK", "Interview readiness", score, score >= 70 ? "RISING" : "STABLE", reason, recommendation);
    }

    private CareerAgentRiskDto.RiskItem executionRisk(CheckInService.CheckInStatus checkIn, List<AgentTask> recentTasks) {
        long total = recentTasks.size();
        long done = recentTasks.stream().filter(task -> "DONE".equals(task.getStatus())).count();
        long dismissed = recentTasks.stream().filter(task -> "DISMISSED".equals(task.getStatus())).count();
        int score = 0;
        if (checkIn.getWeeklyDays() < 3) score += 35;
        if (total >= 3 && done == 0) score += 35;
        if (total > 0 && dismissed * 2 >= total) score += 20;
        if (checkIn.getTodayCompleted() < checkIn.getTodayTotal()) score += 10;
        String reason = score == 0
                ? "Recent check-ins and task completion show stable execution."
                : "Execution risk is elevated: " + checkIn.getWeeklyDays() + "/7 active days, " + done + "/" + total + " agent tasks completed recently.";
        String trend = score >= 60 ? "RISING" : (done > 0 && checkIn.getWeeklyDays() >= 3 ? "DECREASING" : "STABLE");
        return risk("EXECUTION_RISK", "Execution rhythm", score, trend, reason, "Complete one small task today before adding new plans.");
    }

    private CareerAgentRiskDto.RiskItem profileRisk(UserProfileSnapshot.AssessmentBlock assessment,
                                                    UserProfileSnapshot.ResumeBlock resume,
                                                    UserProfileSnapshot.InterviewBlock interview,
                                                    String targetRole) {
        int missing = 0;
        if (assessment == null) missing++;
        if (resume == null) missing++;
        if (interview == null) missing++;
        if (!hasText(targetRole)) missing++;
        int score = missing * 18;
        String reason = missing == 0
                ? "Profile data is broad enough for personalized recommendations."
                : missing + " key profile signals are still missing, so personalization is limited.";
        return risk("PROFILE_RISK", "Personalization quality", score, missing >= 3 ? "RISING" : "STABLE", reason, "Fill the missing profile signals through assessment, resume, and interview modules.");
    }

    private CareerAgentRiskDto.RiskItem risk(String code, String title, int score, String trend, String reason, String recommendation) {
        int normalized = Math.max(0, Math.min(100, score));
        return CareerAgentRiskDto.RiskItem.builder()
                .code(code)
                .title(title)
                .level(riskLevelByScore(normalized))
                .trend(trend)
                .score(normalized)
                .reason(reason)
                .recommendation(recommendation)
                .build();
    }

    private String riskLevelByScore(int score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }

    private int progress(UserProfileSnapshot.AssessmentBlock assessment,
                         UserProfileSnapshot.ResumeBlock resume,
                         UserProfileSnapshot.InterviewBlock interview,
                         String targetRole,
                         CheckInService.CheckInStatus checkIn) {
        int score = 0;
        if (assessment != null) score += 20;
        if (hasText(targetRole)) score += 20;
        if (resume != null) score += 25;
        if (interview != null) score += 25;
        if (checkIn.getWeeklyDays() >= 3) score += 10;
        return Math.min(score, 100);
    }

    private String riskLevel(int riskCount) {
        if (riskCount >= 3) return "HIGH";
        if (riskCount >= 1) return "MEDIUM";
        return "LOW";
    }

    private CareerAgentTodayDto.Action action(String label, String target, String type, String priority) {
        return CareerAgentTodayDto.Action.builder()
                .label(label)
                .target(target)
                .type(type)
                .priority(priority)
                .source("DAILY_AGENT")
                .build();
    }

    private String personalize(String reason, User user) {
        if (user == null || !hasText(user.getMajor())) return reason;
        return reason + " Your major is " + user.getMajor() + ", so recommendations should stay connected to that background.";
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) return value;
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
