package com.example.auth_service.service;

import com.example.auth_service.entity.model.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserEntity save(UserEntity entity);
    UserEntity findByPhone(String phone);
}
