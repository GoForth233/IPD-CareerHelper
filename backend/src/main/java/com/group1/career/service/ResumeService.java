package com.group1.career.service;

import com.group1.career.model.entity.Resume;

import java.util.List;

public interface ResumeService {
    /**
     * @param parsedContent JSON 字符串，存储解析后的简历内容（education/projects/skills/rawContent）
     */
    Resume createResume(Long userId, String title, String targetJob, String fileUrl, String parsedContent);

    Resume getResumeBasic(Long resumeId);

    List<Resume> getUserResumes(Long userId);

    void deleteResume(Long resumeId);

    Resume updateResume(Resume resume);
}
