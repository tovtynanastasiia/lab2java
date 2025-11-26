package com.lab2.service;

import com.lab2.model.TokenData;
import java.util.Map;

public interface TokenService {
    String generateToken(String username, String email, Map<String, Object> metadata);
    TokenData validateToken(String token);
    void invalidateToken(String token);
    String refreshToken(String token);
    boolean isTokenValid(String token);
}

