package com.group1.career.service.impl;

import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.Resume;
import com.group1.career.repository.ResumeRepository;
import com.group1.career.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    @Transactional
    public Resume createResume(Long userId, String title, String targetJob, String fileUrl, String parsedContent) {
        Resume resume = Resume.builder()
                .userId(userId)
                .title(title)
                .targetJob(targetJob)
                .fileUrl(fileUrl)
                .parsedContent(parsedContent)
                .build();
        return resumeRepository.save(resume);
    }

    @Override
    public Resume getResumeBasic(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
    }

    @Override
    public List<Resume> getUserResumes(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteResume(Long resumeId) {
        resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
        resumeRepository.deleteById(resumeId);
        log.info("Deleted resume: {}", resumeId);
    }

    @Override
    @Transactional
    public Resume updateResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public Resume assertOwnership(Long resumeId, Long userId) {
        if (resumeId == null || userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED_ERROR);
        }
        Resume r = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BizException(ErrorCode.RESUME_NOT_FOUND));
        if (!userId.equals(r.getUserId())) {
            log.warn("Ownership violation: user {} tried to access resume {} owned by {}",
                    userId, resumeId, r.getUserId());
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return r;
    }
}
