package com.group1.career.service;

import com.group1.career.model.entity.User;

public interface UserService {
    User register(String nickname, String identityType, String identifier, String credential);
    User login(String identityType, String identifier, String credential);
    User wechatLogin(String code);
    User getUserById(Long userId);
    User updateUser(Long userId, String school, String major, Integer graduationYear);
    void resetPassword(String identifier, String newCredential);
    boolean isEmailRegistered(String email);
}

