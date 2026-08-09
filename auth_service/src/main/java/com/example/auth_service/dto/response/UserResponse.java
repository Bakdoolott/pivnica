package com.example.auth_service.dto.response;

import com.example.auth_service.entity.enums.RoleEnums;

import java.util.Set;

public record UserResponse(
        Long id,
        String phoneNumber,
        String firstName,
        String lastName,
        String email,
        Set<RoleEnums> roles
) {}