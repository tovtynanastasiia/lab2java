package com.lab2.service;

import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;

public interface AuthService {
    AuthResponse register(RegistrationRequest request);
    AuthResponse login(LoginRequest request);
}

