package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RefreshRequest;
import com.example.auth_service.dto.request.VerifyCodeRequest;
import com.example.auth_service.dto.response.TokenResponse;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/mobile/verify-code")
    public ResponseEntity<?> verifyMobileCode(@RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(authService.verify(request.phone(), request.code()));
    }

    @PostMapping("/web/verify-code")
    public ResponseEntity<Void> verifyWebCode(@RequestBody VerifyCodeRequest request){
        TokenResponse token = authService.verify(request.phone(), request.code());

        ResponseCookie accessCookie = ResponseCookie.from("access_token", token.accessToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", token.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
//                .path("/api/v1/auth/web") в prod нужно включить этот путь
                .path("/")
                .maxAge(Duration.ofHours(12))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(UserEntity.builder().phoneNumber(loginRequest.phone()).build()));
    }

    @PostMapping("/web/refresh")
    public ResponseEntity<Void> webRefresh(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        String refreshToken = cookies == null ? null :
                Arrays.stream(cookies)
                        .filter(cookie -> "refresh_token".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authService.refresh(refreshToken).accessToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @PostMapping("/mob/refresh")
    public ResponseEntity<TokenResponse> mobileRefresh(@RequestBody RefreshRequest request){
        return new ResponseEntity<>(authService.refresh(request.refreshToken()), HttpStatus.OK);
    }

    @PostMapping("/mobile/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/web/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        String refreshToken = cookies == null ? null :
                Arrays.stream(cookies)
                        .filter(cookie -> "refresh_token".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        authService.logout(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth/web")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}
