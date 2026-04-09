package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Tag(name = "Auth API", description = "Authentication and Authorization Endpoints")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register User with Validation")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterDto request) {
        return Result.success(userService.register(
                request.getNickname(),
                request.getIdentityType(),
                request.getIdentifier(),
                request.getCredential()
        ));
    }

    @Operation(summary = "Login User")
    @PostMapping("/login")
    public Result<LoginResponseDto> login(@Valid @RequestBody LoginDto request) {
        User user = userService.login(
                request.getIdentityType(),
                request.getIdentifier(),
                request.getCredential()
        );
        String token = com.group1.career.utils.JwtUtils.generateToken(user.getUserId(), "USER");
        return Result.success(new LoginResponseDto(token, user));
    }

    // ================= DTO Classes with JSR 303 Validation =================

    @Data
    public static class LoginResponseDto {
        private String token;
        private User user;

        public LoginResponseDto(String token, User user) {
            this.token = token;
            this.user = user;
        }
    }

    @Data
    public static class RegisterDto {
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")
        private String nickname;

        @NotBlank(message = "Identity Type is required (e.g., MOBILE, EMAIL)")
        private String identityType;

        @NotBlank(message = "Account identifier cannot be blank")
        private String identifier;

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String credential;
    }

    @Data
    public static class LoginDto {
        @NotBlank(message = "Identity Type is required")
        private String identityType;

        @NotBlank(message = "Account identifier cannot be blank")
        private String identifier;

        @NotBlank(message = "Password cannot be blank")
        private String credential;
    }
}
