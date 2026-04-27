package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get User Info (Redis Cache)")
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "Update User Profile (school / major / graduationYear)")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserDto dto) {
        return Result.success(userService.updateUser(id, dto.getSchool(), dto.getMajor(), dto.getGraduationYear()));
    }

    @Data
    public static class UpdateUserDto {
        private String school;
        private String major;
        private Integer graduationYear;
    }
}