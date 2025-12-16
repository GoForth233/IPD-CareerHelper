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

