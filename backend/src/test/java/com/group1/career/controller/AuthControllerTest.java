package com.group1.career.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.common.ErrorCode;
import com.group1.career.exception.BizException;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
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

    @Autowired
    private ObjectMapper objectMapper;

    // ===== Register Tests =====

    @Test
    @DisplayName("POST /auth/register - Success")
    public void testRegister_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "Alice");
        request.put("identityType", "EMAIL");
        request.put("identifier", "alice@example.com");
        request.put("credential", "pass123");

        User mockUser = User.builder().userId(1L).nickname("Alice").build();
        when(userService.register(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("Alice"));
    }

    @Test
    @DisplayName("POST /auth/register - Fail: nickname too short (validation)")
    public void testRegister_NicknameTooShort() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "A");   // min 2 chars
        request.put("identityType", "EMAIL");
        request.put("identifier", "alice@example.com");
        request.put("credential", "pass123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Fail: credential too short (validation)")
    public void testRegister_CredentialTooShort() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "Alice");
        request.put("identityType", "MOBILE");
        request.put("identifier", "13800138000");
        request.put("credential", "abc");  // min 6 chars

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Fail: missing required field")
    public void testRegister_MissingIdentityType() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "Alice");
        // identityType omitted
        request.put("identifier", "alice@example.com");
        request.put("credential", "pass123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Fail: user already exists")
    public void testRegister_UserAlreadyExists() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "Alice");
        request.put("identityType", "EMAIL");
        request.put("identifier", "existing@example.com");
        request.put("credential", "pass123");

        when(userService.register(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new BizException(ErrorCode.USER_ALREADY_EXISTS));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_EXISTS.getCode()));
    }

    // ===== Login Tests =====

    @Test
    @DisplayName("POST /auth/login - Success: returns token and user")
    public void testLogin_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("identityType", "MOBILE");
        request.put("identifier", "13800138000");
        request.put("credential", "password123");

        User mockUser = User.builder().userId(1L).nickname("TestUser").build();
        when(userService.login(anyString(), anyString(), anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.userId").value(1))
                .andExpect(jsonPath("$.data.user.nickname").value("TestUser"));
    }

    @Test
    @DisplayName("POST /auth/login - Fail: missing credential (validation)")
    public void testLogin_MissingCredential() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("identityType", "EMAIL");
        request.put("identifier", "test@example.com");
        // credential omitted

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Fail: user not found")
    public void testLogin_UserNotFound() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("identityType", "EMAIL");
        request.put("identifier", "nobody@example.com");
        request.put("credential", "wrongpass");

        when(userService.login(anyString(), anyString(), anyString()))
                .thenThrow(new BizException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }
}
