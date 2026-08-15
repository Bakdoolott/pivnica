package com.github.bakdoolott.coreservice.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserResolver {
    public Long getUserId(Authentication authentication){
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        }catch (NumberFormatException e){
            throw new AccessDeniedException("Некорректный идентификатор пользователя");
        }
    }
}
