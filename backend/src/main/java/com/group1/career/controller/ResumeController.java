package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.document.ResumeDocument;
import com.group1.career.model.entity.Resume;
import com.group1.career.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Resume API")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "Create Resume (MySQL + MongoDB)")
    @PostMapping
    public Result<ResumeVO> createResume(@RequestBody CreateResumeRequest request) {
        // 1. Call Service to create resume
        Resume resume = resumeService.createResume(
                request.getUserId(),
                request.getTitle(),
                request.getTargetJob(),
                request.getFileUrl(),
                request.getDetail()
        );

        // 2. Construct complete object with Detail for return
        return Result.success(convertToVO(resume, request.getDetail()));
    }

    @Operation(summary = "Get Resume Info (MySQL + Mongo Detail)")
    @GetMapping("/{resumeId}")
    public Result<ResumeVO> getResume(@PathVariable Long resumeId) {
        // 1. Get MySQL basic info
        Resume resume = resumeService.getResumeWithDetailCheck(resumeId);

        // 2. Get MongoDB detail info
        ResumeDocument detail = resumeService.getResumeDetail(resume.getMongoDocId());

        // 3. Merge and return
        return Result.success(convertToVO(resume, detail));
    }

    @Operation(summary = "Get User Resumes")
    @GetMapping("/user/{userId}")
    public Result<List<ResumeVO>> getUserResumes(@PathVariable Long userId) {
        List<Resume> resumes = resumeService.getUserResumes(userId);

        // Note: If the list interface needs details, it will loop through the database query, which may cause performance issues.
        // Here, to match the frontend interface definition, we temporarily do simple aggregation.
        // If the list does not need to show details, it is recommended that the frontend modify the Interface or the backend return null detail
        List<ResumeVO> resumeVOs = resumes.stream().map(resume -> {
            // Here for performance, the list page temporarily does not check Mongo Detail. If the frontend list clicks on details, getResume will be called separately.
            // Or if the frontend requires it, you can uncomment the below, but it is recommended to get detail only on the detail page.
            // ResumeDocument detail = resumeService.getResumeDetail(resume.getMongoDocId());
            // return convertToVO(resume, detail);
            return convertToVO(resume, null);
        }).collect(Collectors.toList());

        return Result.success(resumeVOs);
    }

    @Operation(summary = "Delete Resume (MySQL + MongoDB + OSS)")
    @DeleteMapping("/{resumeId}")
    public Result<String> deleteResume(@PathVariable Long resumeId) {
        resumeService.deleteResume(resumeId);
        return Result.success("Resume deleted successfully");
    }

    @Operation(summary = "Update Resume Metadata")
    @PutMapping("/{resumeId}")
    public Result<ResumeVO> updateResume(@PathVariable Long resumeId, @RequestBody UpdateResumeRequest request) {
        Resume resume = resumeService.getResumeWithDetailCheck(resumeId);
        if (request.getTitle() != null) resume.setTitle(request.getTitle());
        if (request.getTargetJob() != null) resume.setTargetJob(request.getTargetJob());
        if (request.getFileUrl() != null) resume.setFileUrl(request.getFileUrl());
        Resume updated = resumeService.updateResume(resume);
        return Result.success(convertToVO(updated, null));
    }

    // ================= Helper Methods =================

    private ResumeVO convertToVO(Resume resume, ResumeDocument detail) {
        ResumeVO vo = new ResumeVO();
        BeanUtils.copyProperties(resume, vo); // Copy MySQL fields
        vo.setDetail(detail);                 // Set Mongo fields
        return vo;
    }

    // ================= DTO Classes =================

    @Data
    public static class CreateResumeRequest {
        private Long userId;
        private String title;
        private String targetJob;
        private String fileUrl;
        private ResumeDocument detail;
    }

    @Data
    public static class UpdateResumeRequest {
        private String title;
        private String targetJob;
        private String fileUrl;
    }

    /**
     * View Object that matches Frontend Interface
     */
    @Data
    public static class ResumeVO {
        private Long resumeId;
        private Long userId;
        private String title;
        private String targetJob;
        private String fileUrl;
        private String status;
        private String mongoDocId;

        // This matches 'detail: ResumeDetail' in frontend
        private ResumeDocument detail;
    }
}