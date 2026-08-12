package com.example.auth_service.entity.enums;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum RoleEnums implements GrantedAuthority {
    USER,
    ADMIN,
    OWNER;

    @Override
    public @Nullable String getAuthority() {
        return "ROLE_" + name();
    }
}
