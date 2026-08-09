package com.example.auth_service.service;

import com.example.auth_service.entity.model.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserEntity save(UserEntity entity);
    UserEntity findByPhoneNumber(String phoneNumber);
    UserEntity update(UserEntity entity);
    String delete(Long id);
    UserEntity findById(Long id);
}
