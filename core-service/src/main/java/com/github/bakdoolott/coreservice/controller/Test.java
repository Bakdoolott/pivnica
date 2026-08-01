package com.github.bakdoolott.coreservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core")
public class Test {
    @GetMapping("/test")
    ResponseEntity<String> test(){
        return new ResponseEntity<>("test string", HttpStatus.OK);
    }
}
