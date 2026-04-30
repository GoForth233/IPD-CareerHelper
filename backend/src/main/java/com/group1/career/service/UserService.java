package com.group1.career.service;

import com.group1.career.model.entity.User;

public interface UserService {
    User register(String nickname, String identityType, String identifier, String credential);
    User login(String identityType, String identifier, String credential);
    User wechatLogin(String code);
    User getUserById(Long userId);

    /**
     * Patch user profile. Any non-null arg overrides the current value;
     * pass {@code null} for fields that should stay untouched.
     * @param avatarUrl OSS object key (e.g. {@code avatars/uuid.jpg}); the
     *                  field is named {@code avatarUrl} for column-name parity.
     * @param nickname  display name
     */
    User updateUser(Long userId, String nickname, String avatarUrl,
                    String school, String major, Integer graduationYear);

    void resetPassword(String identifier, String newCredential);
    boolean isEmailRegistered(String email);

    /**
     * Populate the transient {@code avatarViewUrl} with a short-lived signed
     * URL. Returns the same instance for chaining; mutates in place. Safe with
     * null input.
     */
    User hydrateUrl(User user);
}

