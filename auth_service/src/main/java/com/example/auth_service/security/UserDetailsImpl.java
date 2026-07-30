package com.example.auth_service.security;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String firstName;
    private String phoneNumber;
    private Set<RoleEnums> roles;
    private boolean enabled;

    public static UserDetailsImpl build(UserEntity user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getFirstName(),
                user.getPhoneNumber(),
                user.getRoles(),
                user.isEnable()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
