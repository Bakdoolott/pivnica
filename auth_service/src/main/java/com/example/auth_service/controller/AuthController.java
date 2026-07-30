package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.UserRequest;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody UserRequest request) {
        return ResponseEntity.ok(authService.verify(request.phone(), request.code()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(UserEntity.builder().phoneNumber(loginRequest.phone()).build()));
    }
}
