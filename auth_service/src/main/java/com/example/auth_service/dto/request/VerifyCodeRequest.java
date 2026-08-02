package com.example.auth_service.dto.request;

public record VerifyCodeRequest
        (String phone,
         String code){}
