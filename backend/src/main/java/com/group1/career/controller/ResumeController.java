package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.Resume;
import com.group1.career.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Resume API")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "Create Resume")
    @PostMapping
    public Result<Resume> createResume(@RequestBody CreateResumeRequest request) {
        Resume resume = resumeService.createResume(
                request.getUserId(),
                request.getTitle(),
                request.getTargetJob(),
                request.getFileUrl(),
                request.getParsedContent()
        );
        return Result.success(resume);
    }

    @Operation(summary = "Get Resume by ID")
    @GetMapping("/{resumeId}")
    public Result<Resume> getResume(@PathVariable Long resumeId) {
        return Result.success(resumeService.getResumeBasic(resumeId));
    }

    @Operation(summary = "Get all resumes for a user")
    @GetMapping("/user/{userId}")
    public Result<List<Resume>> getUserResumes(@PathVariable Long userId) {
        return Result.success(resumeService.getUserResumes(userId));
    }

    @Operation(summary = "Delete Resume")
    @DeleteMapping("/{resumeId}")
    public Result<String> deleteResume(@PathVariable Long resumeId) {
        resumeService.deleteResume(resumeId);
        return Result.success("Resume deleted successfully");
    }

    @Operation(summary = "Update Resume Metadata")
    @PutMapping("/{resumeId}")
    public Result<Resume> updateResume(@PathVariable Long resumeId, @RequestBody UpdateResumeRequest request) {
        Resume resume = resumeService.getResumeBasic(resumeId);
        if (request.getTitle() != null) resume.setTitle(request.getTitle());
        if (request.getTargetJob() != null) resume.setTargetJob(request.getTargetJob());
        if (request.getFileUrl() != null) resume.setFileUrl(request.getFileUrl());
        if (request.getParsedContent() != null) resume.setParsedContent(request.getParsedContent());
        return Result.success(resumeService.updateResume(resume));
    }

    // ================= DTO Classes =================

    @Data
    public static class CreateResumeRequest {
        private Long userId;
        private String title;
        private String targetJob;
        private String fileUrl;
        /** JSON 字符串，包含 education/projects/skills/rawContent */
        private String parsedContent;
    }

    @Data
    public static class UpdateResumeRequest {
        private String title;
        private String targetJob;
        private String fileUrl;
        private String parsedContent;
    }
}
