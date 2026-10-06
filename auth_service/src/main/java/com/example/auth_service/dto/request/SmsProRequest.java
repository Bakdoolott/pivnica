package com.example.auth_service.dto.request;

public record SmsProRequest(
        boolean success,
        int status,
        String message)
{}
