package com.group1.career.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.group1.career.service.EmailService;
import com.group1.career.service.VerificationCodeService;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private VerificationCodeService verificationCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("API Test: Register - Success")
    public void testRegister_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setNickname("TestUser");

        when(verificationCodeService.verify(anyString(), anyString(), anyString())).thenReturn(true);
        when(userService.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockUser);

        Map<String, String> request = Map.of(
                "nickname", "TestUser",
                "identityType", "EMAIL_PASSWORD",
                "identifier", "test@example.com",
                "credential", "password123",
                "code", "123456"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.nickname").value("TestUser"));
    }

    @Test
    @DisplayName("API Test: Register - Invalid Email")
    public void testRegister_InvalidEmail() throws Exception {
        Map<String, String> request = Map.of(
                "nickname", "TestUser",
                "identityType", "EMAIL_PASSWORD",
                "identifier", "not-an-email",
                "credential", "password123",
                "code", "123456"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API Test: Register - Short Password")
    public void testRegister_ShortPassword() throws Exception {
        Map<String, String> request = Map.of(
                "nickname", "TestUser",
                "identityType", "EMAIL_PASSWORD",
                "identifier", "test@example.com",
                "credential", "123",
                "code", "123456"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API Test: Login - Success")
    public void testLogin_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setNickname("TestUser");

        when(userService.login(anyString(), anyString(), anyString()))
                .thenReturn(mockUser);

        Map<String, String> request = Map.of(
                "identityType", "EMAIL_PASSWORD",
                "identifier", "test@example.com",
                "credential", "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.userId").value(1L));
    }

    @Test
    @DisplayName("API Test: Login - Missing Fields")
    public void testLogin_MissingFields() throws Exception {
        Map<String, String> request = Map.of(
                "identityType", "EMAIL_PASSWORD"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API Test: WeChat Login - Success")
    public void testWechatLogin_Success() throws Exception {
        User mockUser = new User();
        mockUser.setUserId(2L);
        mockUser.setNickname("WeChatUser");

        when(userService.wechatLogin(anyString())).thenReturn(mockUser);

        Map<String, String> request = Map.of("code", "wx_mock_code_123");

        mockMvc.perform(post("/auth/wechat-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.userId").value(2L));
    }

    @Test
    @DisplayName("API Test: WeChat Login - Missing Code")
    public void testWechatLogin_MissingCode() throws Exception {
        Map<String, String> request = Map.of();

        mockMvc.perform(post("/auth/wechat-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
