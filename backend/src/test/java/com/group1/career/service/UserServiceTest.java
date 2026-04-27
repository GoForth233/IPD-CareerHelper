package com.group1.career.service;

import com.group1.career.model.entity.Role;
import com.group1.career.model.entity.User;
import com.group1.career.model.entity.UserAuth;
import com.group1.career.repository.RoleRepository;
import com.group1.career.repository.UserAuthRepository;
import com.group1.career.repository.UserRepository;
import com.group1.career.repository.UserRoleRepository;
import com.group1.career.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthRepository userAuthRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Test User Registration - Success")
    public void testRegister_Success() {
        String nickname = "TestUser";
        String identityType = "EMAIL_PASSWORD";
        String identifier = "test@example.com";
        String credential = "password123";

        User mockUser = User.builder().userId(1L).nickname(nickname).build();

        when(userAuthRepository.findByIdentifierAndIdentityType(any(), any()))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userAuthRepository.save(any(UserAuth.class))).thenReturn(new UserAuth());
        when(roleRepository.findByRoleCode("STUDENT")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");

        User result = userService.register(nickname, identityType, identifier, credential);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(nickname, result.getNickname());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userAuthRepository, times(1)).save(any(UserAuth.class));
    }

    @Test
    @DisplayName("Test Get User By ID - Fetch from DB")
    public void testGetUserById_FromDb() {
        Long userId = 1L;
        User mockUser = User.builder().userId(userId).nickname("TestUser").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).findById(userId);
    }
}
