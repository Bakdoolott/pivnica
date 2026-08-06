package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UserRequest;
import com.example.auth_service.dto.request.UserUpdateRequest;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.entity.model.UserEntity;import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;
    private final UserMapper mapper;
    private final AuthService authService;

    @PutMapping("/update-user")
    public ResponseEntity<UserResponse> update(@Valid @RequestBody UserUpdateRequest request){
        System.out.println(request.toString());
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
}
