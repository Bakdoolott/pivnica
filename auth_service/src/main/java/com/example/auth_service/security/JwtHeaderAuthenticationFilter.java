package com.example.auth_service.security;

import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final UserService userService;

    public JwtHeaderAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);

        if (userId != null && !userId.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserEntity user = userService.findById(Long.valueOf(userId));
                UserDetailsImpl userDetails = UserDetailsImpl.build(user);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Кривой id, либо пользователь удалён/не найден -> oстаёмся анонимом.
                // Защищённые эндпоинты ответят 401/403.
            }
        }

        filterChain.doFilter(request, response);
    }
}
