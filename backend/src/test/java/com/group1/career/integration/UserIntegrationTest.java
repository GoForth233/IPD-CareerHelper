package com.group1.career.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration Test: Tests the complete HTTP -> Controller -> Service -> DB flow.
 * register/login are now under AuthController (/auth/*).
 */
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
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "IntegrationTestUser");
        request.put("identityType", "PASSWORD");
        request.put("identifier", "integration-test@example.com");
        request.put("credential", "test123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("IntegrationTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Login with Existing User")
    public void testLoginFlow() throws Exception {
        // Step 1: Register user
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("nickname", "LoginTestUser");
        registerRequest.put("identityType", "PASSWORD");
        registerRequest.put("identifier", "login-test@example.com");
        registerRequest.put("credential", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Step 2: Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("identityType", "PASSWORD");
        loginRequest.put("identifier", "login-test@example.com");
        loginRequest.put("credential", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.nickname").value("LoginTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Prevent Duplicate Registration")
    public void testDuplicateRegistrationPrevention() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nickname", "DuplicateTestUser");
        request.put("identityType", "PASSWORD");
        request.put("identifier", "duplicate@example.com");
        request.put("credential", "pass123");

        // First registration succeeds
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Second registration with same identifier should return business error
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
