package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.CareerNode;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.Interview;
import com.group1.career.model.entity.InterviewQuestion;
import com.group1.career.model.entity.Organization;
import com.group1.career.model.entity.User;
import com.group1.career.repository.CareerNodeRepository;
import com.group1.career.repository.CareerPathRepository;
import com.group1.career.repository.InterviewQuestionRepository;
import com.group1.career.repository.InterviewRepository;
import com.group1.career.repository.OrganizationRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.service.AdminAuthService;
import com.group1.career.service.WeeklyReportService;
import com.group1.career.utils.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * B-side console endpoints. Every endpoint requires the caller to hold
 * the {@code ADMIN} role; we check via {@link AdminAuthService#requireAdmin}
 * rather than Spring Security {@code @PreAuthorize} (the rest of the app
 * uses a custom JWT pipeline so we keep the check inline).
 */
@Slf4j
@Tag(name = "Admin API", description = "Admin-only ops endpoints (Sprint D-4/D-5)")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final WeeklyReportService weeklyReportService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final CareerPathRepository careerPathRepository;
    private final CareerNodeRepository careerNodeRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final ObjectMapper objectMapper;

    // ─────────────────── Auth-shaped utilities ───────────────────

    @Operation(summary = "Whoami — confirms the caller is recognised as an admin")
    @GetMapping("/whoami")
    public Result<Boolean> whoami() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(adminAuthService.isAdmin(uid));
    }

    // ─────────────────── Weekly report manual triggers ───────────────────

    @Operation(summary = "Manually trigger the weekly report job for every active user")
    @PostMapping("/weekly-report/run")
    public Result<WeeklyReportService.RunSummary> runWeeklyReport() {
        requireAdmin();
        return Result.success(weeklyReportService.runForAll());
    }

    @Operation(summary = "Manually trigger the weekly report for a specific user (debug)")
    @PostMapping("/weekly-report/run-user")
    public Result<Boolean> runWeeklyReportForUser(@RequestParam Long userId) {
        requireAdmin();
        return Result.success(weeklyReportService.runForUser(userId));
    }

    // ─────────────────── Organizations CRUD ───────────────────

    @Operation(summary = "List organizations")
    @GetMapping("/organizations")
    public Result<List<Organization>> listOrganizations() {
        requireAdmin();
        return Result.success(organizationRepository.findAll());
    }

    @Operation(summary = "Create or update an organization (upsert by code)")
    @PostMapping("/organizations")
    public Result<Organization> saveOrganization(@RequestBody Organization payload) {
        requireAdmin();
        if (payload.getCode() == null || payload.getCode().isBlank()) {
            throw new BizException("Organization code is required");
        }
        Organization existing = organizationRepository.findByCode(payload.getCode().trim()).orElse(null);
        if (existing != null) {
            existing.setName(payload.getName());
            existing.setDescription(payload.getDescription());
            existing.setContactName(payload.getContactName());
            existing.setContactEmail(payload.getContactEmail());
            if (payload.getActive() != null) existing.setActive(payload.getActive());
            return Result.success(organizationRepository.save(existing));
        }
        payload.setCode(payload.getCode().trim());
        if (payload.getActive() == null) payload.setActive(true);
        return Result.success(organizationRepository.save(payload));
    }

    // ─────────────────── Org dashboard ───────────────────

    @Operation(summary = "Org dashboard — radar averages, interview counts, weak dimensions")
    @GetMapping("/organizations/{orgId}/dashboard")
    public Result<OrgDashboardDto> orgDashboard(@PathVariable Long orgId) {
        requireAdmin();
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new BizException("Organization not found"));

        List<User> students = userRepository.findByOrgId(orgId);
        Map<String, Double[]> dimSums = new HashMap<>();
        for (String d : RADAR_DIMENSIONS) dimSums.put(d, new Double[]{0.0, 0.0}); // sum, count
        int interviewCount = 0;
        int reportCount = 0;
        for (User u : students) {
            for (Interview iv : interviewRepository.findByUserIdOrderByStartedAtDesc(u.getUserId())) {
                interviewCount++;
                if (iv.getReportJson() == null || iv.getReportJson().isBlank()) continue;
                try {
                    JsonNode radar = objectMapper.readTree(iv.getReportJson()).get("radarChart");
                    if (radar == null || !radar.isObject()) continue;
                    reportCount++;
                    for (String d : RADAR_DIMENSIONS) {
                        JsonNode v = radar.get(d);
                        if (v == null || !v.isNumber()) continue;
                        Double[] cell = dimSums.get(d);
                        cell[0] += v.asDouble();
                        cell[1] += 1;
                    }
                } catch (Exception e) {
                    log.debug("[admin] could not parse report for interview {}: {}", iv.getInterviewId(), e.toString());
                }
            }
        }
        Map<String, Double> averages = new HashMap<>();
        for (String d : RADAR_DIMENSIONS) {
            Double[] cell = dimSums.get(d);
            averages.put(d, cell[1] == 0 ? 0.0 : Math.round((cell[0] / cell[1]) * 10.0) / 10.0);
        }
        // Weak dimensions = lowest 3 averages.
        List<String> weak = new ArrayList<>(averages.keySet());
        weak.sort((a, b) -> Double.compare(averages.get(a), averages.get(b)));
        List<String> top3Weak = weak.size() > 3 ? weak.subList(0, 3) : weak;

        return Result.success(OrgDashboardDto.builder()
                .orgId(orgId)
                .studentCount(students.size())
                .interviewCount(interviewCount)
                .reportCount(reportCount)
                .radarAverages(averages)
                .weakDimensionsTop3(top3Weak)
                .build());
    }

    @Operation(summary = "List students in an org with their interview counts and last score")
    @GetMapping("/organizations/{orgId}/students")
    public Result<List<StudentRowDto>> orgStudents(@PathVariable Long orgId) {
        requireAdmin();
        List<StudentRowDto> rows = new ArrayList<>();
        for (User u : userRepository.findByOrgId(orgId)) {
            List<Interview> ivs = interviewRepository.findByUserIdOrderByStartedAtDesc(u.getUserId());
            Integer lastScore = ivs.stream()
                    .filter(i -> i.getFinalScore() != null)
                    .findFirst()
                    .map(Interview::getFinalScore)
                    .orElse(null);
            rows.add(StudentRowDto.builder()
                    .userId(u.getUserId())
                    .nickname(u.getNickname())
                    .school(u.getSchool())
                    .major(u.getMajor())
                    .interviewCount(ivs.size())
                    .lastInterviewScore(lastScore)
                    .build());
        }
        return Result.success(rows);
    }

    @Operation(summary = "Student detail — full interview list with scores")
    @GetMapping("/students/{userId}")
    public Result<StudentDetailDto> studentDetail(@PathVariable Long userId) {
        requireAdmin();
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("Student not found"));
        return Result.success(StudentDetailDto.builder()
                .user(u)
                .interviews(interviewRepository.findByUserIdOrderByStartedAtDesc(userId))
                .build());
    }

    // ─────────────────── Skill map editor ───────────────────

    @Operation(summary = "List all career paths (admin)")
    @GetMapping("/career-paths")
    public Result<List<CareerPath>> listPaths() {
        requireAdmin();
        return Result.success(careerPathRepository.findAll());
    }

    @Operation(summary = "Create or update a career path (upsert by id; new on null)")
    @PostMapping("/career-paths")
    public Result<CareerPath> savePath(@RequestBody CareerPath payload) {
        requireAdmin();
        return Result.success(careerPathRepository.save(payload));
    }

    @Operation(summary = "Delete a career path")
    @DeleteMapping("/career-paths/{pathId}")
    public Result<Void> deletePath(@PathVariable Integer pathId) {
        requireAdmin();
        careerPathRepository.deleteById(pathId);
        return Result.success();
    }

    @Operation(summary = "List nodes in a path (admin — full tree, no progress join)")
    @GetMapping("/career-paths/{pathId}/nodes")
    public Result<List<CareerNode>> listNodes(@PathVariable Integer pathId) {
        requireAdmin();
        return Result.success(careerNodeRepository.findByPathIdOrderBySortOrderAsc(pathId));
    }

    @Operation(summary = "Create or update a node")
    @PostMapping("/career-paths/{pathId}/nodes")
    public Result<CareerNode> saveNode(@PathVariable Integer pathId, @RequestBody CareerNode payload) {
        requireAdmin();
        payload.setPathId(pathId);
        return Result.success(careerNodeRepository.save(payload));
    }

    @Operation(summary = "Delete a node")
    @DeleteMapping("/career-paths/nodes/{nodeId}")
    public Result<Void> deleteNode(@PathVariable Long nodeId) {
        requireAdmin();
        careerNodeRepository.deleteById(nodeId);
        return Result.success();
    }

    // ─────────────────── Question bank moderation ───────────────────

    @Operation(summary = "List all questions (admin — includes HIDDEN status)")
    @GetMapping("/questions")
    public Result<List<InterviewQuestion>> listQuestions() {
        requireAdmin();
        return Result.success(interviewQuestionRepository.findAll());
    }

    @Operation(summary = "Update a question (status, content, position, difficulty)")
    @PostMapping("/questions/{id}")
    public Result<InterviewQuestion> updateQuestion(@PathVariable Long id, @RequestBody InterviewQuestion payload) {
        requireAdmin();
        InterviewQuestion existing = interviewQuestionRepository.findById(id)
                .orElseThrow(() -> new BizException("Question not found"));
        if (payload.getContent() != null) existing.setContent(payload.getContent());
        if (payload.getSummary() != null) existing.setSummary(payload.getSummary());
        if (payload.getPosition() != null) existing.setPosition(payload.getPosition());
        if (payload.getDifficulty() != null) existing.setDifficulty(payload.getDifficulty());
        if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        return Result.success(interviewQuestionRepository.save(existing));
    }

    @Operation(summary = "Delete a question")
    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        requireAdmin();
        interviewQuestionRepository.deleteById(id);
        return Result.success();
    }

    // ─────────────────── Helpers + DTOs ───────────────────

    private static final List<String> RADAR_DIMENSIONS = List.of(
            "expression", "logic", "technical", "pressureResistance", "communication"
    );

    private void requireAdmin() {
        Long uid = SecurityUtil.requireCurrentUserId();
        adminAuthService.requireAdmin(uid);
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class OrgDashboardDto {
        private Long orgId;
        private int studentCount;
        private int interviewCount;
        private int reportCount;
        private Map<String, Double> radarAverages;
        private List<String> weakDimensionsTop3;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class StudentRowDto {
        private Long userId;
        private String nickname;
        private String school;
        private String major;
        private int interviewCount;
        private Integer lastInterviewScore;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class StudentDetailDto {
        private User user;
        private List<Interview> interviews;
    }
}
