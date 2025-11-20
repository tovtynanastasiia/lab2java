package com.lab2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class InfoController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Auth Application");
        info.put("status", "running");
        info.put("description", "Spring Boot application for user registration and authentication");
        info.put("endpoints", Map.of(
            "register", "POST /api/auth/register",
            "login", "POST /api/auth/login",
            "apiInfo", "GET /api/auth/"
        ));
        info.put("note", "Use POST method with JSON body for register and login. You can also visit /api/auth/register or /api/auth/login with GET to see examples.");
        return ResponseEntity.ok(info);
    }
}

