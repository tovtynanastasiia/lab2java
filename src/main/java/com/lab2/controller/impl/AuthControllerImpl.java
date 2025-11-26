package com.lab2.controller.impl;

import com.lab2.annotation.RequiresToken;
import com.lab2.controller.AuthController;
import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;
import com.lab2.service.AuthService;
import com.lab2.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final TokenService tokenService;

    @Autowired
    public AuthControllerImpl(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
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
        logger.info("Отримано запит на реєстрацію");
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Помилки валідації запиту на реєстрацію: {}", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Помилка валідації: " + errors, null));
        }
        
        logger.info("Запит на реєстрацію пройшов початкову валідацію, передано в сервіс");
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            logger.info("Реєстрація успішна");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            logger.warn("Реєстрація не вдалася: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        logger.info("Отримано запит на авторизацію для користувача: {}", request.getUsername());
        
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            logger.warn("Помилки валідації запиту на авторизацію: {}", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(false, "Помилка валідації: " + errors, null));
        }
        
        logger.info("Запит на авторизацію пройшов початкову валідацію, передано в сервіс");
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            logger.info("Авторизація успішна для користувача: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Авторизація не вдалася: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/token/refresh")
    @RequiresToken
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request) {
        logger.info("Отримано запит на оновлення токену");
        
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : null;
        
        try {
            String newToken = tokenService.refreshToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Токен успішно оновлено");
            response.put("token", newToken);
            
            logger.info("Токен успішно оновлено");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Помилка при оновленні токену: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Помилка при оновленні токену: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/token/invalidate")
    @RequiresToken
    public ResponseEntity<Map<String, Object>> invalidateToken(HttpServletRequest request) {
        logger.info("Отримано запит на інвалідацію токену");
        
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : null;
        
        try {
            tokenService.invalidateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Токен успішно інвалідовано");
            
            logger.info("Токен успішно інвалідовано");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Помилка при інвалідації токену: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Помилка при інвалідації токену: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

