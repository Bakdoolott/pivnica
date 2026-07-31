package com.example.auth_service.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}

