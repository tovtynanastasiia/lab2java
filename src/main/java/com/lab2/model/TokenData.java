package com.lab2.model;

import java.time.LocalDateTime;
import java.util.Map;

public class TokenData {
    private String username;
    private String email;
    private String applicationName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Map<String, Object> metadata;

    public TokenData() {
    }

    public TokenData(String username, String email, String applicationName, 
                     LocalDateTime createdAt, LocalDateTime expiresAt, 
                     Map<String, Object> metadata) {
        this.username = username;
        this.email = email;
        this.applicationName = applicationName;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.metadata = metadata;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}

