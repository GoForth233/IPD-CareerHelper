package com.group1.career.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.career.controller.UserController;
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

/**
 * Integration Test Example
 * 
 * This tests the complete flow from HTTP request -> Controller -> Service -> Repository -> Database
 * It uses a real Spring context and can connect to a test database.
 * 
 * Note: For production-ready integration tests, consider using:
 * - @Testcontainers for Docker-based test databases
 * - H2 in-memory database for faster tests
 * - Test profiles with separate configurations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback after each test
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integration Test: Complete User Registration Flow")
    public void testCompleteUserRegistrationFlow() throws Exception {
        // Step 1: Register a new user
        UserController.RegisterRequest registerRequest = new UserController.RegisterRequest();
        registerRequest.setNickname("IntegrationTestUser");
        registerRequest.setIdentityType("PASSWORD");
        registerRequest.setIdentifier("integration-test@example.com");
        registerRequest.setCredential("test123");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("IntegrationTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Login with Existing User")
    public void testLoginFlow() throws Exception {
        // Pre-condition: Register user first
        UserController.RegisterRequest registerRequest = new UserController.RegisterRequest();
        registerRequest.setNickname("LoginTestUser");
        registerRequest.setIdentityType("PASSWORD");
        registerRequest.setIdentifier("login-test@example.com");
        registerRequest.setCredential("password123");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Step 2: Login with the registered credentials
        UserController.LoginRequest loginRequest = new UserController.LoginRequest();
        loginRequest.setIdentityType("PASSWORD");
        loginRequest.setIdentifier("login-test@example.com");
        loginRequest.setCredential("password123");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("LoginTestUser"));
    }

    @Test
    @DisplayName("Integration Test: Prevent Duplicate Registration")
    public void testDuplicateRegistrationPrevention() throws Exception {
        // Step 1: Register user
        UserController.RegisterRequest request = new UserController.RegisterRequest();
        request.setNickname("DuplicateTestUser");
        request.setIdentityType("PASSWORD");
        request.setIdentifier("duplicate@example.com");
        request.setCredential("pass123");

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Step 2: Try to register again with same identifier
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // Expect 200 OK but with error code 500 in body
    }
}

