package com.lab2.service;

import com.lab2.model.TokenData;
import com.lab2.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
        try {
            Field secretField = TokenServiceImpl.class.getDeclaredField("secretKey");
            secretField.setAccessible(true);
            secretField.set(tokenService, "testSecretKeyForJWTTokenGenerationAndValidationInAuthApplication2024");
            
            Field expirationField = TokenServiceImpl.class.getDeclaredField("expirationHours");
            expirationField.setAccessible(true);
            expirationField.set(tokenService, 1);
            
            Field appNameField = TokenServiceImpl.class.getDeclaredField("applicationName");
            appNameField.setAccessible(true);
            appNameField.set(tokenService, "Test Auth Application");
        } catch (Exception e) {
            fail("Помилка налаштування тестового середовища: " + e.getMessage());
        }
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String email = "test@example.com";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", 12345L);
        metadata.put("role", "user");

        String token = tokenService.generateToken(username, email, metadata);

        assertNotNull(token, "Токен не повинен бути null");
        assertFalse(token.isEmpty(), "Токен не повинен бути порожнім");
    }

    @Test
    void testValidateToken() {
        String username = "testuser";
        String email = "test@example.com";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", 12345L);

        String token = tokenService.generateToken(username, email, metadata);
        TokenData tokenData = tokenService.validateToken(token);

        assertNotNull(tokenData, "TokenData не повинен бути null");
        assertEquals(username, tokenData.getUsername(), "Username повинен співпадати");
        assertEquals(email, tokenData.getEmail(), "Email повинен співпадати");
        assertEquals("Test Auth Application", tokenData.getApplicationName(), "Назва додатку повинна співпадати");
        assertNotNull(tokenData.getCreatedAt(), "Час створення не повинен бути null");
        assertNotNull(tokenData.getExpiresAt(), "Термін дії не повинен бути null");
        assertNotNull(tokenData.getMetadata(), "Метадані не повинні бути null");
    }

    @Test
    void testInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            tokenService.validateToken(invalidToken);
        }, "Невірний токен повинен викликати виняток");
    }

    @Test
    void testInvalidateToken() {
        String username = "testuser";
        String email = "test@example.com";
        String token = tokenService.generateToken(username, email, null);

        tokenService.invalidateToken(token);

        assertThrows(Exception.class, () -> {
            tokenService.validateToken(token);
        }, "Інвалідований токен повинен викликати виняток при валідації");
    }

    @Test
    void testRefreshToken() {
        String username = "testuser";
        String email = "test@example.com";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", 12345L);

        String oldToken = tokenService.generateToken(username, email, metadata);
        String newToken = tokenService.refreshToken(oldToken);

        assertNotNull(newToken, "Новий токен не повинен бути null");
        assertNotEquals(oldToken, newToken, "Новий токен повинен відрізнятися від старого");

        TokenData newTokenData = tokenService.validateToken(newToken);
        assertEquals(username, newTokenData.getUsername(), "Username повинен зберегтися після оновлення");

        assertThrows(Exception.class, () -> {
            tokenService.validateToken(oldToken);
        }, "Старий токен повинен бути інвалідований");
    }

    @Test
    void testIsTokenValid() {
        String username = "testuser";
        String email = "test@example.com";
        String token = tokenService.generateToken(username, email, null);

        assertTrue(tokenService.isTokenValid(token), "Валідний токен повинен повертати true");

        tokenService.invalidateToken(token);
        assertFalse(tokenService.isTokenValid(token), "Інвалідований токен повинен повертати false");
    }

    @Test
    void testTokenContainsMetadata() {
        String username = "testuser";
        String email = "test@example.com";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", 12345L);
        metadata.put("role", "admin");
        metadata.put("department", "IT");

        String token = tokenService.generateToken(username, email, metadata);
        TokenData tokenData = tokenService.validateToken(token);

        assertNotNull(tokenData.getMetadata(), "Метадані не повинні бути null");
        assertEquals(12345L, tokenData.getMetadata().get("userId"), "userId повинен зберегтися в метаданих");
        assertEquals("admin", tokenData.getMetadata().get("role"), "role повинен зберегтися в метаданих");
        assertEquals("IT", tokenData.getMetadata().get("department"), "department повинен зберегтися в метаданих");
    }
}

