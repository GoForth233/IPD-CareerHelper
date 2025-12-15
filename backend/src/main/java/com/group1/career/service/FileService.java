package com.group1.career.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * Upload file to OSS
     * @param file Multipart file from frontend
     * @param folder Folder name (e.g. "resumes", "avatars")
     * @return File Public URL
     */
    String uploadFile(MultipartFile file, String folder);
}

