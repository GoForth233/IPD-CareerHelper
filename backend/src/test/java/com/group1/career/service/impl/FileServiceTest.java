package com.group1.career.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.group1.career.config.OssConfigProperties;
import com.group1.career.exception.BizException;
import com.group1.career.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private OssConfigProperties ossConfig;

    @Mock
    private OSS ossClient;

    @Mock
    private MultipartFile mockFile;

    private FileServiceImpl fileService;

    @BeforeEach
    public void setUp() {
        // Inject mocked config
        fileService = spy(new FileServiceImpl(ossConfig));
        
        // Mock OSS client creation
        lenient().doReturn(ossClient).when(fileService).createOssClient();
        
        // Mock OSS client operations
        lenient().when(ossClient.putObject(anyString(), anyString(), any(InputStream.class)))
                .thenReturn(new PutObjectResult());
    }

    @Test
    @DisplayName("Test Upload File - Empty File")
    public void testUploadFile_EmptyFile() {
        when(mockFile.isEmpty()).thenReturn(true);

        assertThrows(BizException.class, () -> {
            fileService.uploadFile(mockFile, "resumes");
        });
    }

    @Test
    @DisplayName("Test Upload File - Success")
    public void testUploadFile_Success() throws Exception {
        // Mock config
        when(ossConfig.getBucketName()).thenReturn("test-bucket");
        when(ossConfig.getEndpoint()).thenReturn("oss-cn-test.aliyuncs.com");
        
        // Mock file
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test-resume.pdf");
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        String result = fileService.uploadFile(mockFile, "resumes");

        assertNotNull(result);
        assertTrue(result.startsWith("https://"));
        assertTrue(result.contains("test-bucket"));
        assertTrue(result.contains("resumes/"));
        
        verify(ossClient).putObject(anyString(), anyString(), any(InputStream.class));
        verify(ossClient).shutdown();
    }

    @Test
    @DisplayName("Test Upload File - Null Folder Uses Default")
    public void testUploadFile_NullFolder() throws Exception {
        // Mock config
        when(ossConfig.getBucketName()).thenReturn("test-bucket");
        when(ossConfig.getEndpoint()).thenReturn("oss-cn-test.aliyuncs.com");
        
        // Mock file
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("file.txt");
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        String result = fileService.uploadFile(mockFile, null);

        assertNotNull(result);
        assertTrue(result.contains("others/"));
        
        verify(ossClient).putObject(anyString(), anyString(), any(InputStream.class));
        verify(ossClient).shutdown();
    }

    @Test
    @DisplayName("Test Upload File - Extension Handling")
    public void testUploadFile_ExtensionHandling() throws Exception {
        // Mock config
        when(ossConfig.getBucketName()).thenReturn("test-bucket");
        when(ossConfig.getEndpoint()).thenReturn("oss-cn-test.aliyuncs.com");
        
        // Mock file
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("document.docx");
        InputStream inputStream = new ByteArrayInputStream("doc content".getBytes());
        when(mockFile.getInputStream()).thenReturn(inputStream);

        String result = fileService.uploadFile(mockFile, "documents");

        assertNotNull(result);
        assertTrue(result.endsWith(".docx"));
        
        verify(ossClient).putObject(anyString(), anyString(), any(InputStream.class));
        verify(ossClient).shutdown();
    }
}

