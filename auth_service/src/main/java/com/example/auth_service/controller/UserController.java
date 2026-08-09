package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/users")
public class UserController {

    private final UserService service;
    private final UserMapper mapper;
    private final AuthService authService;
    private final UserMapper userMapper;

    @PutMapping("/update-user/admin")
    public ResponseEntity<UserResponse> updateAdmin(@Valid @RequestBody UserUpdateRequest request){
        return ResponseEntity.ok(
                mapper.toResponseDto(
                    service.update(
                            mapper.toEntity(request)
                    )
                )
        );
    }

    @PutMapping("/update-user")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UserUpdateRequest request){
        request.setId(authService.getCurrentUser().getId());
        return ResponseEntity.ok(
                mapper.toResponseDto(
                        service.update(
                                mapper.toEntity(request)
                        )
                )
        );
    }

    @GetMapping("/get-user")
    public ResponseEntity<UserResponse> getUser(){
        return ResponseEntity.ok(
                mapper.toResponseDto(
                        service.findById(
                                authService.getCurrentUser().getId()
                        )
                )
        );
    }

    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserResponse> getUserId(@PathVariable Long id){
        return ResponseEntity.ok(
                mapper.toResponseDto(
                        service.findById(id)
                )
        );
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser(){
        return ResponseEntity.ok(
                service.delete(
                        authService.getCurrentUser().getId()
                )
        );
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUserId(@PathVariable Long id){
        return ResponseEntity.ok(
                service.delete(id)
        );
    }

    @PutMapping("/update-user-roles/{id}")
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id, @RequestBody Set<RoleEnums> roleEnums){
        return ResponseEntity.ok(
                userMapper.toResponseDto(
                        service.updateRoles(roleEnums, id)
                )
        );
    }

    @PutMapping("/remove-user-roles/{id}")
    public ResponseEntity<UserResponse> removeRoles(@PathVariable Long id, @RequestBody Set<RoleEnums> roleEnums){
        return ResponseEntity.ok(
                userMapper.toResponseDto(
                        service.removeRoles(roleEnums, id)
                )
        );
    }
}
