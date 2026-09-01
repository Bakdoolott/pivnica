package com.example.auth_service.service;

import com.example.auth_service.dto.response.TokenResponse;
import com.example.auth_service.entity.model.UserEntity;

public interface AuthService {
    void generateAndSend(String phone);
    TokenResponse verify(String phone, String code);
    TokenResponse refresh(String refreshToken);
    void logout(String refreshToken);
    String login(UserEntity entity);
    UserEntity getCurrentUser();
    String registration(String phone);   // ← было UserEntity, стало String
}

