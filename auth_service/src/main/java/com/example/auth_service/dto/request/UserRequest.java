package com.example.auth_service.dto.request;

public record UserRequest
        (String phone,
         String code){}
