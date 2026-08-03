package com.github.bakdoolott.coreservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core")
public class Test {
    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<String> test(HttpServletRequest request){
        return new ResponseEntity<>("test string", HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    ResponseEntity<String> userTest(){
        return new ResponseEntity<>("test user", HttpStatus.OK);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    ResponseEntity<String> adminTest() {
        return new ResponseEntity<>("test admin", HttpStatus.OK);
    }

    @GetMapping("/allowed-test")
    @PreAuthorize("permitAll()")
    ResponseEntity<String> allowedTest(){
        return new ResponseEntity<>("allowed test string", HttpStatus.OK);
    }
}