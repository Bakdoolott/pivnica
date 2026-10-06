package com.example.auth_service.service;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {
    UserEntity save(UserEntity entity);
    UserEntity findByPhoneNumber(String phoneNumber);
    UserEntity findOrCreateByPhoneNumber(String phoneNumber);   // ← новый
    UserEntity update(UserEntity entity);
    String delete(Long id);
    UserEntity findById(Long id);
    UserEntity updateRoles(Set<RoleEnums> roleEnums, Long id);
    UserEntity removeRoles(Set<RoleEnums> roleEnums, Long id);
}
