package com.group1.career.controller;

import com.group1.career.interceptor.AuthInterceptor;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserProfileSnapshotService;
import com.group1.career.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// register/login have been moved to AuthController (/auth/*), tested in AuthControllerTest.
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /**
     * Required so the controller can inject this dep when the
     * UserController test slice constructs the bean. The snapshot endpoints
     * aren't exercised in this file, so a default mock (returns null) is fine.
     */
    @MockitoBean
    private UserProfileSnapshotService snapshotService;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    public void bypassAuth() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /users/{id} - Returns user info")
    public void testGetUser_Success() throws Exception {
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setNickname("ExistingUser");

        when(userService.getUserById(userId)).thenReturn(mockUser);
        // Controller hydrates the avatar presigned URL just before returning;
        // the no-op identity stub keeps the test focused on the JSON shape
        // without pulling in the OSS SDK.
        when(userService.hydrateUrl(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.nickname").value("ExistingUser"));
    }
}
