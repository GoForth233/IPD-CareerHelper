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

        // Default folder fallback
        if (folder == null || folder.trim().isEmpty()) {
            folder = "others";
        }

        // 1. Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        
        // Path: folder/uuid.ext
        String fileName = folder + "/" + UUID.randomUUID().toString() + extension;

        // 2. Create OSS Client
        OSS ossClient = createOssClient();

        try {
            // 3. Upload
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());
            
            // 4. Generate Public URL
            // Format: https://{bucket}.{endpoint}/{filename}
            String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + fileName;
            log.info("File uploaded successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new BizException("File upload failed: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
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
        String fileName = folder + "/" + UUID.randomUUID() + extension;

        OSS ossClient = createOssClient();
        try {
            ossClient.putObject(ossConfig.getBucketName(), fileName, new ByteArrayInputStream(bytes));
            String url = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + fileName;
            log.info("Bytes uploaded successfully: {}", url);
            return url;
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

    /**
     * Strip the scheme + host from a stored OSS URL to recover the bare object key.
     * Accepts already-bare keys unchanged.
     */
    private String toObjectKey(String fileUrlOrKey) {
        String s = fileUrlOrKey.trim();
        int idx = s.indexOf("://");
        if (idx < 0) return s.startsWith("/") ? s.substring(1) : s;
        int slash = s.indexOf('/', idx + 3);
        if (slash < 0 || slash + 1 >= s.length()) {
            throw new BizException("Malformed OSS URL: " + fileUrlOrKey);
        }
        return s.substring(slash + 1);
    }

    /**
     * Create OSS client - extracted for testability
     */
    protected OSS createOssClient() {
        return new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
    }
}

