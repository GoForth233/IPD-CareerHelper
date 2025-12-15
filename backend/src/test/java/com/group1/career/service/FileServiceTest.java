package com.group1.career.service;

import com.group1.career.config.OssConfigProperties;
import com.group1.career.exception.BizException;
import com.group1.career.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private OssConfigProperties ossConfig;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private FileServiceImpl fileService;

    @BeforeEach
    public void setUp() {
        when(ossConfig.getEndpoint()).thenReturn("oss-cn-test.aliyuncs.com");
        when(ossConfig.getAccessKeyId()).thenReturn("test-key-id");
        when(ossConfig.getAccessKeySecret()).thenReturn("test-key-secret");
        when(ossConfig.getBucketName()).thenReturn("test-bucket");
    }

    @Test
    @DisplayName("Test Upload File - Empty File")
    public void testUploadFile_EmptyFile() {
        // Prepare
        when(mockFile.isEmpty()).thenReturn(true);

        // Execute & Verify
        assertThrows(BizException.class, () -> {
            fileService.uploadFile(mockFile, "resumes");
        });
    }

    @Test
    @DisplayName("Test Upload File - Success")
    public void testUploadFile_Success() throws Exception {
        // Prepare
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test-resume.pdf");
        
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // Note: This test is simplified because mocking OSS client is complex
        // In real scenarios, you might want to use integration tests with testcontainers
        // or mock the OSS client behavior more thoroughly
        
        // For now, we test the validation logic
        String result = fileService.uploadFile(mockFile, "resumes");

        // Verify URL format
        assertNotNull(result);
        assertTrue(result.startsWith("https://"));
        assertTrue(result.contains("test-bucket"));
        assertTrue(result.contains("resumes/"));
    }

    @Test
    @DisplayName("Test Upload File - Null Folder Uses Default")
    public void testUploadFile_NullFolder() throws Exception {
        // Prepare
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("file.txt");
        
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // Execute
        String result = fileService.uploadFile(mockFile, null);

        // Verify it uses "others" as default folder
        assertNotNull(result);
        assertTrue(result.contains("others/"));
    }

    @Test
    @DisplayName("Test Upload File - Extension Handling")
    public void testUploadFile_ExtensionHandling() throws Exception {
        // Prepare
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("document.docx");
        
        InputStream inputStream = new ByteArrayInputStream("doc content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        // Execute
        String result = fileService.uploadFile(mockFile, "documents");

        // Verify URL contains extension
        assertNotNull(result);
        assertTrue(result.endsWith(".docx"));
    }
}

