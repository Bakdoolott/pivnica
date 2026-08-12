package com.example.auth_service.config;

import com.example.auth_service.security.JwtHeaderAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter;

    public SecurityConfig(JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter) {
        this.jwtHeaderAuthenticationFilter = jwtHeaderAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        //AuthController — публичные: аутентификация по коду / по refresh-токену.
                        //logout тоже публичный: gateway отдаёт /api/v1/auth/auth/** как public и
                        //НЕ проставляет X-User-Id, а сам logout аутентифицируется владением
                        //refresh-токеном (cookie/тело), а не SecurityContext-ом.
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/auth/login",
                                "/api/v1/auth/auth/mobile/verify-code",
                                "/api/v1/auth/auth/web/verify-code",
                                "/api/v1/auth/auth/mobile/refresh",
                                "/api/v1/auth/auth/web/refresh",
                                "/api/v1/auth/auth/mobile/logout",
                                "/api/v1/auth/auth/web/logout"
                        ).permitAll()

                        //UserController — авторизация по ролям (роли берутся из БД фильтром).
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/auth/users/update-user-roles/{id}",
                                "/api/v1/auth/users/remove-user-roles/{id}")
                                .hasAnyRole("OWNER")
                        .requestMatchers(
                                "/api/v1/auth/users/delete-user/{id}",
                                "/api/v1/auth/users/get-user/{id}",
                                "/api/v1/auth/users/update-user/admin")
                                .hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(
                                "/api/v1/auth/users/update-user",
                                "/api/v1/auth/users/delete-user")
                                .authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
