package com.example.auth_service.dto.request;

import com.example.auth_service.entity.enums.RoleEnums;
import jakarta.validation.constraints.Email;

import java.util.Set;

public record UserRequest(
        String firstName,
        String lastName,
        Set<RoleEnums> role,
        @Email(message = "Некоректный формат email")
        String email
) {}
