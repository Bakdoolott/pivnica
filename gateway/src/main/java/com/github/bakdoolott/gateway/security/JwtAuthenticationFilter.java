package com.github.bakdoolott.gateway.security;

import com.github.bakdoolott.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        System.out.println("PATH = " + request.getURI().getPath());
        String path = request.getURI().getPath();

        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Roles");
                });

        if (isPublicPath(path)) {
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }


        String token = extractToken(request);

        if (token == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        try {
            if (!jwtUtil.isTokenValid(token)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtUtil.extractAllClaims(token);

            Object userId = claims.get("userId");
            Object roles = claims.get("roles");

            if (userId != null && roles != null) {
                requestBuilder.header("X-User-Id", userId.toString());
                requestBuilder.header("X-User-Roles", roles.toString());
            }


            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());

        } catch (JwtException e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private String extractToken(ServerHttpRequest request) {

        // 1. Сначала Authorization (для мобильных клиентов)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Затем HttpOnly Cookie (для браузера)
        HttpCookie cookie = request.getCookies().getFirst("access_token");

        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    private boolean isPublicPath(String path) {
        return path.contains("/api/v1/auth")
                || path.equals("/swagger-ui.html")
                || path.equals("/swagger-ui/index.html")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/webjars/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.matches("/docs/.*/v3/api-docs.*");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
