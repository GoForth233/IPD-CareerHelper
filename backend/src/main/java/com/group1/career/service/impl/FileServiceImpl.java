package com.group1.career.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.group1.career.config.OssConfigProperties;
import com.group1.career.exception.BizException;
import com.group1.career.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final OssConfigProperties ossConfig;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new BizException("File cannot be empty");
        }
        if (folder == null || folder.trim().isEmpty()) folder = "others";

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String key = folder + "/" + UUID.randomUUID() + extension;

        OSS ossClient = createOssClient();
        try {
            ossClient.putObject(ossConfig.getBucketName(), key, file.getInputStream());
            log.info("File uploaded: bucket={} key={} size={}B", ossConfig.getBucketName(), key, file.getSize());
            return key;
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BizException("File upload failed: " + e.getMessage());
        } finally {
            if (ossClient != null) ossClient.shutdown();
        }
    }

    @Override
    public String uploadBytes(byte[] bytes, String filename, String folder) {
        if (bytes == null || bytes.length == 0) {
            throw new BizException("File bytes cannot be empty");
        }
        if (folder == null || folder.trim().isEmpty()) folder = "others";

        String extension = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf("."))
                : "";
        String key = folder + "/" + UUID.randomUUID() + extension;

        OSS ossClient = createOssClient();
        try {
            ossClient.putObject(ossConfig.getBucketName(), key, new ByteArrayInputStream(bytes));
            log.info("Bytes uploaded: bucket={} key={} size={}B", ossConfig.getBucketName(), key, bytes.length);
            return key;
        } finally {
            if (ossClient != null) ossClient.shutdown();
        }
    }

    @Override
    public byte[] downloadBytes(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.isBlank()) {
            throw new BizException("Empty file reference");
        }
        String objectKey = toObjectKey(fileUrlOrKey);
        OSS ossClient = createOssClient();
        try (com.aliyun.oss.model.OSSObject obj = ossClient.getObject(ossConfig.getBucketName(), objectKey);
             java.io.InputStream in = obj.getObjectContent()) {
            return in.readAllBytes();
        } catch (com.aliyun.oss.OSSException | com.aliyun.oss.ClientException e) {
            log.error("OSS download failed: bucket={}, key={}", ossConfig.getBucketName(), objectKey, e);
            throw new BizException("Failed to fetch file from OSS: " + e.getMessage());
        } catch (IOException e) {
            log.error("OSS stream read failed: key={}", objectKey, e);
            throw new BizException("Failed to read OSS stream: " + e.getMessage());
        } finally {
            if (ossClient != null) ossClient.shutdown();
        }
    }

    @Override
    public String presignedUrl(String fileUrlOrKey, long ttlSeconds) {
        if (fileUrlOrKey == null || fileUrlOrKey.isBlank()) return null;
        // Clamp to a reasonable window so a misuse (e.g. ttl=0 or 1 year)
        // can't either generate immediately-expired links or open a huge
        // hot-link window if the bucket policy ever loosens.
        long ttl = Math.max(60, Math.min(ttlSeconds, 86400));
        String key = toObjectKey(fileUrlOrKey);
        OSS ossClient = createOssClient();
        try {
            Date expiry = new Date(System.currentTimeMillis() + ttl * 1000L);
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), key, expiry);
            return url.toString();
        } catch (com.aliyun.oss.OSSException | com.aliyun.oss.ClientException e) {
            log.warn("Failed to presign OSS url for key={}: {}", key, e.getMessage());
            return null;
        } finally {
            if (ossClient != null) ossClient.shutdown();
        }
    }

    /**
     * Strip the scheme + host from a stored OSS URL to recover the bare object key.
     * Accepts already-bare keys unchanged. Tolerates leading slashes.
     */
    private String toObjectKey(String fileUrlOrKey) {
        String s = fileUrlOrKey.trim();
        int idx = s.indexOf("://");
        if (idx < 0) return s.startsWith("/") ? s.substring(1) : s;
        int slash = s.indexOf('/', idx + 3);
        if (slash < 0 || slash + 1 >= s.length()) {
            throw new BizException("Malformed OSS URL: " + fileUrlOrKey);
        }
        // Strip query string (presigned URLs can round-trip through us)
        String key = s.substring(slash + 1);
        int q = key.indexOf('?');
        return q >= 0 ? key.substring(0, q) : key;
    }

    /** Create OSS client - extracted for testability */
    protected OSS createOssClient() {
        // Always use HTTPS so presigned URLs are accepted by WeChat mini-program
        // (which rejects plain HTTP media URLs) and by browser security policies.
        String endpoint = ossConfig.getEndpoint();
        if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            endpoint = "https://" + endpoint;
        } else if (endpoint.startsWith("http://")) {
            endpoint = "https://" + endpoint.substring(7);
        }
        return new OSSClientBuilder().build(
                endpoint,
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
    }
}

