package com.group1.career.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Thin abstraction over Aliyun OSS. The contract intentionally returns
 * **object keys** (e.g. {@code resumes/uuid.pdf}) rather than absolute URLs
 * so downstream code (DB rows, AI pipelines) does not become coupled to a
 * specific bucket / endpoint / CDN. Callers who need a browser-loadable
 * link should call {@link #presignedUrl(String, long)} just-in-time.
 */
public interface FileService {
    /**
     * Upload a multipart file to OSS.
     * @param file   incoming upload
     * @param folder OSS folder prefix (e.g. {@code resumes}, {@code avatars})
     * @return the bare object key, e.g. {@code resumes/uuid.pdf}
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * Upload raw bytes (e.g. an AI-generated PDF) to OSS.
     * @param bytes    file content
     * @param filename original filename, used only to derive the extension
     * @param folder   OSS folder prefix
     * @return the bare object key
     */
    String uploadBytes(byte[] bytes, String filename, String folder);

    /**
     * Download an OSS-stored object back to bytes using authenticated SDK access.
     * The bucket is private; anonymous HTTP GET would return AccessDenied.
     * @param fileUrlOrKey full https URL (legacy callers) or a bare object key
     * @return raw object bytes
     */
    byte[] downloadBytes(String fileUrlOrKey);

    /**
     * Issue a short-lived signed URL the frontend / WeChat MP can load directly.
     * Useful for displaying avatars and previewing PDFs without proxying through us.
     * Returns {@code null} for null/blank keys so callers can blindly map over a list.
     *
     * @param fileUrlOrKey object key (preferred) or legacy full URL
     * @param ttlSeconds   how long the URL remains valid; clamped to [60, 86400]
     * @return https URL with signature & expiry, or {@code null}
     */
    String presignedUrl(String fileUrlOrKey, long ttlSeconds);

    /**
     * Delete an object from OSS. Idempotent: a missing or null key is a no-op,
     * and OSS itself does not 404 on missing keys. Implementations log and
     * swallow non-fatal errors so callers can call this in cleanup paths
     * without rolling back the originating transaction.
     */
    void deleteObject(String fileUrlOrKey);
}

