package com.lab2.controller.impl;

import com.lab2.controller.AuthController;
import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;
import com.lab2.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthControllerImpl.class);
    
    private final AuthService authService;

    @Autowired
    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(value = {"", "/"})
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "Auth API");
        info.put("status", "running");
        info.put("endpoints", Map.of(
            "register", "POST /api/auth/register",
            "login", "POST /api/auth/login"
        ));
        info.put("note", "Use POST method with JSON body for register and login endpoints");
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/register")
    public ResponseEntity<Map<String, Object>> registerInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("error", "Method not allowed");
        info.put("message", "This endpoint only accepts POST requests");
        info.put("method", "POST");
        info.put("url", "/api/auth/register");
        info.put("example", Map.of(
            "username", "testuser",
            "password", "Test123!@#",
            "email", "test@example.com",
            "birthday", "2000-01-01",
            "phoneNumber", "+380501234567"
        ));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(info);
    }
    
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> loginInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("error", "Method not allowed");
        info.put("message", "This endpoint only accepts POST requests");
        info.put("method", "POST");
        info.put("url", "/api/auth/login");
        info.put("example", Map.of(
            "username", "testuser",
            "password", "Test123!@#"
        ));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(info);
    }

    @PostMapping("/register")
    @Override
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request, BindingResult bindingResult) {
        logger.info("Received registration request");
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Registration request validation errors: {}", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Validation failed: " + errors, null));
        }
        
        logger.info("Registration request passed initial validation, forwarding to service");
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            logger.info("Registration successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            logger.warn("Registration failed: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        logger.info("Received login request for username: {}", request.getUsername());
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Login request validation errors: {}", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Validation failed: " + errors, null));
        }
        
        logger.info("Login request passed initial validation, forwarding to service");
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            logger.info("Login successful for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Login failed: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

