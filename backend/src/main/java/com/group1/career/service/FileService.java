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

    /**
     * Upload raw bytes (e.g. AI-generated PDF) to OSS.
     * @param bytes file content
     * @param filename original filename, used to derive extension
     * @param folder OSS folder prefix
     * @return public URL
     */
    String uploadBytes(byte[] bytes, String filename, String folder);

    /**
     * Download an OSS-stored object back to bytes using authenticated SDK access.
     * Required because the bucket is private — anonymous GET on the file URL returns AccessDenied.
     * @param fileUrlOrKey full https URL produced by uploadFile/uploadBytes, or a bare object key
     * @return raw object bytes
     */
    byte[] downloadBytes(String fileUrlOrKey);
}

