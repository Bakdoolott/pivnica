package com.example.auth_service.service;

import com.example.auth_service.entity.model.UserEntity;

public interface AuthService {
    void generateAndSend(String phone, Long chatId);
    String verify(String phone, String code);
    String login(UserEntity entity);
    UserEntity getCurrentUser();
}
