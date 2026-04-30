package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.exception.BizException;
import com.group1.career.model.dto.UserProfileSnapshot;
import com.group1.career.model.entity.User;
import com.group1.career.service.UserProfileSnapshotService;
import com.group1.career.service.UserService;
import com.group1.career.utils.SecurityUtil;
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
    private final UserProfileSnapshotService snapshotService;

    @Operation(summary = "Get user profile (presigned avatar URL hydrated)")
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.hydrateUrl(userService.getUserById(id)));
    }

    /**
     * Patch the caller's own profile. Any field omitted from the body is left
     * untouched. {@code avatarUrl} is the OSS object key returned by
     * {@code POST /api/files/upload?folder=avatars}.
     *
     * Cross-user updates are blocked: if {@code id} doesn't match the JWT
     * subject we throw FORBIDDEN. The previous version trusted the path
     * variable, which would have let any authenticated user overwrite any
     * profile.
     */
    @Operation(summary = "Update own profile (nickname / avatar / school / major / graduation year)")
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserDto dto) {
        Long uid = SecurityUtil.requireCurrentUserId();
        if (!uid.equals(id)) {
            throw new BizException(com.group1.career.common.ErrorCode.FORBIDDEN);
        }
        User updated = userService.updateUser(
                id, dto.getNickname(), dto.getAvatarUrl(),
                dto.getSchool(), dto.getMajor(), dto.getGraduationYear());
        return Result.success(userService.hydrateUrl(updated));
    }

    /**
     * The cross-tool user portrait. Returned as the typed {@link UserProfileSnapshot}
     * (not the raw column) so the frontend never has to parse the JSON column itself
     * and gets nullable blocks for free. Always returns a snapshot, even an empty one,
     * so the caller never has to NPE-guard.
     */
    @Operation(summary = "Get the caller's cross-tool profile snapshot")
    @GetMapping("/me/profile-snapshot")
    public Result<UserProfileSnapshot> getMyProfileSnapshot() {
        Long uid = SecurityUtil.requireCurrentUserId();
        return Result.success(snapshotService.read(uid));
    }

    /**
     * Patch the caller's preferences block (target role / interview mode).
     * Only the fields present in the body are applied — useful when the
     * frontend records the user's chosen interview mode or wants to set
     * a target role from the assessment result CTA.
     */
    @Operation(summary = "Update preferences block in the profile snapshot")
    @PutMapping("/me/profile-snapshot/preferences")
    public Result<UserProfileSnapshot> updatePreferences(@RequestBody UpdatePreferencesDto dto) {
        Long uid = SecurityUtil.requireCurrentUserId();
        snapshotService.mergePreferences(uid, UserProfileSnapshot.PreferencesBlock.builder()
                .targetRole(dto.getTargetRole())
                .interviewMode(dto.getInterviewMode())
                .build());
        return Result.success(snapshotService.read(uid));
    }

    @Data
    public static class UpdateUserDto {
        private String nickname;
        /** OSS object key from /api/files/upload?folder=avatars */
        private String avatarUrl;
        private String school;
        private String major;
        private Integer graduationYear;
    }

    @Data
    public static class UpdatePreferencesDto {
        private String targetRole;
        /** "voice" or "text" */
        private String interviewMode;
    }
}