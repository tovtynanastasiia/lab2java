package com.lab2.service.impl;

import com.lab2.model.TokenData;
import com.lab2.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TokenServiceImpl implements TokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration.hours}")
    private int expirationHours;
    
    @Value("${jwt.application.name}")
    private String applicationName;
    
    private final Set<String> invalidatedTokens = new HashSet<>();
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(String username, String email, Map<String, Object> metadata) {
        logger.info("Генерація токену для користувача: {}", username);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(expirationHours);
        
        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date expiration = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("email", email);
        claims.put("applicationName", applicationName);
        claims.put("createdAt", now.toString());
        claims.put("expiresAt", expiresAt.toString());
        if (metadata != null) {
            claims.putAll(metadata);
        }
        
        String token = Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
        
        logger.info("Токен успішно згенеровано для користувача: {}. Термін дії: {} годин", username, expirationHours);
        return token;
    }

    @Override
    public TokenData validateToken(String token) {
        logger.debug("Перевірка валідності токену");
        
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Спроба перевірки порожнього токену");
            throw new IllegalArgumentException("Токен не може бути порожнім");
        }
        
        if (invalidatedTokens.contains(token)) {
            logger.warn("Спроба використання інвалідованого токену");
            throw new IllegalStateException("Токен було інвалідовано");
        }
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String username = claims.getSubject();
            String email = claims.get("email", String.class);
            String appName = claims.get("applicationName", String.class);
            String createdAtStr = claims.get("createdAt", String.class);
            String expiresAtStr = claims.get("expiresAt", String.class);
            
            LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
            LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr);
            
            Map<String, Object> metadata = new HashMap<>();
            claims.forEach((key, value) -> {
                if (!key.equals("username") && !key.equals("email") && 
                    !key.equals("applicationName") && !key.equals("createdAt") && 
                    !key.equals("expiresAt") && !key.equals("sub") && 
                    !key.equals("iat") && !key.equals("exp")) {
                    metadata.put(key, value);
                }
            });
            
            TokenData tokenData = new TokenData(username, email, appName, createdAt, expiresAt, metadata);
            
            if (LocalDateTime.now().isAfter(expiresAt)) {
                logger.warn("Токен прострочено для користувача: {}", username);
                throw new IllegalStateException("Токен прострочено");
            }
            
            logger.info("Токен успішно перевірено для користувача: {}", username);
            return tokenData;
            
        } catch (Exception e) {
            logger.error("Помилка при перевірці токену: {}", e.getMessage());
            throw new IllegalStateException("Невірний токен: " + e.getMessage());
        }
    }

    @Override
    public void invalidateToken(String token) {
        logger.info("Інвалідація токену");
        
        if (token == null || token.trim().isEmpty()) {
            logger.warn("Спроба інвалідації порожнього токену");
            throw new IllegalArgumentException("Токен не може бути порожнім");
        }
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String username = claims.getSubject();
            invalidatedTokens.add(token);
            logger.info("Токен успішно інвалідовано для користувача: {}", username);
            
        } catch (Exception e) {
            logger.error("Помилка при інвалідації токену: {}", e.getMessage());
            throw new IllegalStateException("Невірний токен для інвалідації: " + e.getMessage());
        }
    }

    @Override
    public String refreshToken(String token) {
        logger.info("Оновлення токену");
        
        TokenData tokenData = validateToken(token);
        
        Map<String, Object> metadata = tokenData.getMetadata() != null ? tokenData.getMetadata() : new HashMap<>();
        
        String newToken = generateToken(tokenData.getUsername(), tokenData.getEmail(), metadata);
        
        invalidateToken(token);
        
        logger.info("Токен успішно оновлено для користувача: {}", tokenData.getUsername());
        return newToken;
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            logger.debug("Токен невалідний: {}", e.getMessage());
            return false;
        }
    }
}

