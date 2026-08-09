package com.example.auth_service.config;

import com.example.auth_service.security.JwtCore;
import com.example.auth_service.security.TokenFilter;
import com.example.auth_service.service.UserService;
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

    private final JwtCore jwtCore;
    private final UserService userDetailsService;

    public SecurityConfig(JwtCore jwtCore, UserService userDetailsService) {
        this.jwtCore = jwtCore;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        //AuthContoller
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/mobile/verify-code",
                                "/api/v1/auth/web/verify-code",
                                "/api/v1/auth/mob/refresh",
                                "/api/v1/auth/web/refresh"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/mobile/logout",
                                "/api/v1/auth/web/logout"
                        ).authenticated()
                        //UserController
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
                .addFilterBefore(new TokenFilter(jwtCore, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}