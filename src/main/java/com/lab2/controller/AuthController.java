package com.lab2.controller;

import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface AuthController {
    ResponseEntity<AuthResponse> register(RegistrationRequest request, BindingResult bindingResult);
    ResponseEntity<AuthResponse> login(LoginRequest request, BindingResult bindingResult);
}

