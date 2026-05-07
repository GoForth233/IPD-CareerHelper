package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Resume;
import com.group1.career.service.AiService;
import com.group1.career.service.FileService;
import com.group1.career.service.ResumeService;
import com.group1.career.utils.PdfTextExtractor;
import com.group1.career.utils.SecurityUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

/**
 * AI-driven resume generation:
 *   POST /api/resume-gen/from-template   — generate from structured form data
 *   POST /api/resume-gen/tailor          — re-write an existing resume against a JD
 * Both pipelines: AI -> HTML -> PDF (OpenHTMLtoPDF) -> OSS upload -> persist Resume row.
 */
@Slf4j
@Tag(name = "Resume Generation API", description = "AI-driven resume creation & tailoring")
@RestController
@RequestMapping("/api/resume-gen")
@RequiredArgsConstructor
public class ResumeGenController {

    private final AiService aiService;
    private final ResumeService resumeService;
    private final FileService fileService;

    @Operation(summary = "Generate a brand-new resume from template form data")
    @PostMapping("/from-template")
    // 不加 @Transactional：同 tailor，AI 调用期间不应持有数据库事务
    public Result<Resume> fromTemplate(@RequestBody TemplateRequest req) {
        Long uid = SecurityUtil.requireCurrentUserId();
        if (req.getName() == null || req.getName().isBlank()) throw new BizException("name is required");

        String prompt = buildTemplatePrompt(req);
        String html = callAiForHtml(prompt);

        String fileKey = htmlToPdfAndUpload(html, "resumes/generated");
        String title = (req.getTargetRole() == null ? req.getName() : req.getName() + "_" + req.getTargetRole())
                .replaceAll("\\s+", "_");
        Resume saved = resumeService.createResume(uid, title, req.getTargetRole(),
                fileKey, null);
        return Result.success(resumeService.hydrateUrl(saved));
    }

    @Operation(summary = "Tailor an existing resume against a Job Description")
    @PostMapping("/tailor")
    // 不加 @Transactional：AI rewrite + PDF render 耗时 30-90s，持有事务会耗尽连接池
    // assertOwnership / createResume 各自在自己的短事务中完成
    public Result<Resume> tailor(@RequestBody TailorRequest req) {
        Long uid = SecurityUtil.requireCurrentUserId();
        if (req.getResumeId() == null) throw new BizException("resumeId is required");

        long t0 = System.currentTimeMillis();
        // Source resume must belong to the caller
        Resume base = resumeService.assertOwnership(req.getResumeId(), uid);

        String resumeText = base.getParsedContent();
        if ((resumeText == null || resumeText.isBlank()) && base.getFileUrl() != null) {
            long ts = System.currentTimeMillis();
            byte[] pdfBytes = fileService.downloadBytes(base.getFileUrl());
            resumeText = PdfTextExtractor.extractFromBytes(pdfBytes);
            log.info("[tailor] PDF download+extract took {} ms ({} bytes -> {} chars)",
                    System.currentTimeMillis() - ts, pdfBytes.length, resumeText == null ? 0 : resumeText.length());
        }
        if (resumeText == null || resumeText.isBlank()) {
            throw new BizException("Source resume has no readable content");
        }

        String prompt = buildTailorPrompt(resumeText, req.getJobDescription() == null ? "" : req.getJobDescription());
        long ts = System.currentTimeMillis();
        String html = callAiForHtml(prompt);
        log.info("[tailor] AI rewrite took {} ms ({} html chars)", System.currentTimeMillis() - ts, html.length());

        ts = System.currentTimeMillis();
        String fileKey = htmlToPdfAndUpload(html, "resumes/tailored");
        log.info("[tailor] PDF render+OSS upload took {} ms", System.currentTimeMillis() - ts);

        String title = (base.getTitle() == null ? "Resume" : base.getTitle()) + "_tailored";
        Resume saved = resumeService.createResume(uid, title,
                req.getJobDescription() != null && req.getJobDescription().length() > 60
                        ? req.getJobDescription().substring(0, 60) : req.getJobDescription(),
                fileKey, null);
        log.info("[tailor] DONE total {} ms, resumeId={}", System.currentTimeMillis() - t0, saved.getResumeId());
        return Result.success(resumeService.hydrateUrl(saved));
    }

    // ========================= Internals =========================

    private String buildTemplatePrompt(TemplateRequest r) {
        return "You are an expert resume writer. Produce a professional one-page resume in HTML " +
               "(no <html>/<head> wrapper, only body content using <h1>, <h2>, <ul>, <li>, <p>, <strong>). " +
               "Use the STAR method to expand the experience section. Polish wording. " +
               "Return ONLY HTML, no markdown fences.\n\n" +
               "Name: " + safe(r.getName()) + "\n" +
               "Phone: " + safe(r.getPhone()) + "\n" +
               "Email: " + safe(r.getEmail()) + "\n" +
               "Target Role: " + safe(r.getTargetRole()) + "\n" +
               "Preferred City: " + safe(r.getCity()) + "\n" +
               "University: " + safe(r.getUniversity()) + "\n" +
               "Major: " + safe(r.getMajor()) + "\n" +
               "Degree: " + safe(r.getDegree()) + "\n" +
               "Graduation Year: " + safe(r.getGraduationYear()) + "\n" +
               "Skills: " + safe(r.getSkills()) + "\n" +
               "Experience:\n" + safe(r.getExperience());
    }

    private String buildTailorPrompt(String resumeText, String jd) {
        return "You are an expert resume writer. Rewrite the following resume to maximize its alignment " +
               "with the target job description. Keep all factual content but reorder, rephrase, and " +
               "highlight the most relevant skills/projects. Output as professional one-page HTML " +
               "(body content only: <h1>, <h2>, <ul>, <li>, <p>, <strong>). Return ONLY HTML.\n\n" +
               "=== Job Description ===\n" + jd + "\n\n" +
               "=== Source Resume ===\n" + resumeText;
    }

    private String callAiForHtml(String prompt) {
        String reply = aiService.chat(prompt);
        if (reply == null || reply.isBlank()) {
            throw new BizException("AI returned empty response");
        }
        // Strip ``` fences if model added them despite instructions
        String trimmed = reply.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) trimmed = trimmed.substring(firstNewline + 1);
            int closingFence = trimmed.lastIndexOf("```");
            if (closingFence > 0) trimmed = trimmed.substring(0, closingFence);
        }
        return trimmed;
    }

    private String htmlToPdfAndUpload(String htmlBody, String folder) {
        // Wrap into a minimal valid XHTML doc for the renderer
        String wrapped = "<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\"><head>" +
                "<meta charset=\"UTF-8\"/><style>" +
                "body{font-family:'Helvetica',sans-serif;color:#222;line-height:1.45;font-size:11pt;margin:36pt}" +
                "h1{font-size:20pt;margin:0 0 6pt;color:#1e3a8a}" +
                "h2{font-size:13pt;margin:14pt 0 4pt;color:#1e40af;border-bottom:1pt solid #1e40af;padding-bottom:2pt}" +
                "ul{margin:4pt 0 4pt 16pt;padding:0}li{margin:2pt 0}" +
                "p{margin:4pt 0}strong{color:#0f172a}" +
                "</style></head><body>" + sanitizeForXhtml(htmlBody) + "</body></html>";

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(wrapped, null);
            builder.toStream(bos);
            builder.run();
            byte[] pdfBytes = bos.toByteArray();
            String filename = "ai-resume-" + System.currentTimeMillis() + ".pdf";
            return fileService.uploadBytes(pdfBytes, filename, folder);
        } catch (Exception e) {
            log.error("PDF render/upload failed", e);
            throw new BizException("Resume PDF generation failed: " + e.getMessage());
        }
    }

    private String sanitizeForXhtml(String html) {
        // Self-close common void elements that LLMs sometimes leave open
        return html
                .replaceAll("(?i)<br\\s*>", "<br/>")
                .replaceAll("(?i)<hr\\s*>", "<hr/>");
    }

    private String stripTags(String html) {
        return html == null ? "" : html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String safe(Object v) { return v == null ? "" : v.toString(); }

    // ========================= DTOs =========================

    @Data
    public static class TemplateRequest {
        private Long userId;
        private String name;
        private String phone;
        private String email;
        private String targetRole;
        private String city;
        private String university;
        private String major;
        private String degree;
        private String graduationYear;
        private String skills;
        private String experience;
    }

    @Data
    public static class TailorRequest {
        private Long userId;
        private Long resumeId;
        private String jobDescription;
    }
}
