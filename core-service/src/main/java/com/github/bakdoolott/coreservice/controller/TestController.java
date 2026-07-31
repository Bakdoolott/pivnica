package com.github.bakdoolott.coreservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/core")
public class TestController {
    @GetMapping("/get")
    @PreAuthorize("authenticated")
    ResponseEntity<String> get(){
        return new ResponseEntity<>("Hello mother fucker", HttpStatus.OK);
    }
}
