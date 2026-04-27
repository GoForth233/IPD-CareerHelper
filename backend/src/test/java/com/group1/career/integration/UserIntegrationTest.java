package com.group1.career.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.controller.AuthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integration Test: Complete User Registration Flow")
    public void testCompleteUserRegistrationFlow() throws Exception {
        AuthController.RegisterDto registerRequest = new AuthController.RegisterDto();
        registerRequest.setNickname("IntegrationTestUser");
        registerRequest.setIdentityType("EMAIL_PASSWORD");
        registerRequest.setIdentifier("integration-test@example.com");
        registerRequest.setCredential("test123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("IntegrationTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Login with Existing User")
    public void testLoginFlow() throws Exception {
        AuthController.RegisterDto registerRequest = new AuthController.RegisterDto();
        registerRequest.setNickname("LoginTestUser");
        registerRequest.setIdentityType("EMAIL_PASSWORD");
        registerRequest.setIdentifier("login-test@example.com");
        registerRequest.setCredential("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        AuthController.LoginDto loginRequest = new AuthController.LoginDto();
        loginRequest.setIdentityType("EMAIL_PASSWORD");
        loginRequest.setIdentifier("login-test@example.com");
        loginRequest.setCredential("password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.nickname").value("LoginTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Prevent Duplicate Registration")
    public void testDuplicateRegistrationPrevention() throws Exception {
        AuthController.RegisterDto request = new AuthController.RegisterDto();
        request.setNickname("DuplicateTestUser");
        request.setIdentityType("EMAIL_PASSWORD");
        request.setIdentifier("duplicate@example.com");
        request.setCredential("pass123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
