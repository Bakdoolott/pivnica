package com.github.bakdoolott.gateway.security;

import com.github.bakdoolott.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtUtil jwtUtil) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .addFilterAt(jwtAuthenticationFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/docs/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }

    private AuthenticationWebFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtAuthenticationManager(jwtUtil));
        filter.setServerAuthenticationConverter(bearerTokenConverter());
        filter.setRequiresAuthenticationMatcher(exchange -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicPath(path)) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }

            return ServerWebExchangeMatcher.MatchResult.match();
        });
        return filter;
    }

    private ReactiveAuthenticationManager jwtAuthenticationManager(JwtUtil jwtUtil) {
        return authentication -> {
            String token = authentication.getCredentials().toString();

            if (!jwtUtil.isTokenValid(token)) {
                return Mono.error(new BadCredentialsException("Invalid JWT"));
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String principal = extractPrincipal(claims);
            Collection<GrantedAuthority> authorities = extractAuthorities(claims);

            return Mono.just(new UsernamePasswordAuthenticationToken(principal, token, authorities));
        };
    }

    private ServerAuthenticationConverter bearerTokenConverter() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.empty();
            }

            String token = authHeader.substring(7);
            return Mono.just(new UsernamePasswordAuthenticationToken(null, token));
        };
    }

    private String extractPrincipal(Claims claims) {
        Object userId = claims.get("userId");

        if (userId != null) {
            return userId.toString();
        }

        return claims.getSubject();
    }

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object roles = claims.get("roles");

        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::toAuthority)
                    .toList();
        }

        if (roles instanceof String rolesText) {
            return Arrays.stream(rolesText.replace("[", "").replace("]", "").split(","))
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::toAuthority)
                    .toList();
        }

        return List.of();
    }

    private GrantedAuthority toAuthority(String role) {
        if (role.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(role);
        }

        return new SimpleGrantedAuthority("ROLE_" + role);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/webjars/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/docs/");
    }
}
