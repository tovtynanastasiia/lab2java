package com.lab2.service.impl;

import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;
import com.lab2.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Override
    public AuthResponse register(RegistrationRequest request) {
        logger.info("Starting registration process for username: {}", request.getUsername());
        
        String validationError = validateRegistration(request);
        if (validationError != null) {
            logger.warn("Registration validation failed: {}", validationError);
            return new AuthResponse(false, validationError, null);
        }
        
        logger.info("Registration validation successful for username: {}", request.getUsername());
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("userId", 12345L);
        mockData.put("username", request.getUsername());
        mockData.put("email", request.getEmail());
        mockData.put("registeredAt", LocalDate.now().toString());
        
        logger.info("Registration completed successfully for username: {}", request.getUsername());
        return new AuthResponse(true, "User registered successfully", mockData);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Starting login process for username: {}", request.getUsername());
        
        String validationError = validateLogin(request);
        if (validationError != null) {
            logger.warn("Login validation failed: {}", validationError);
            return new AuthResponse(false, validationError, null);
        }
        
        logger.info("Login validation successful for username: {}", request.getUsername());
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("token", "mock-jwt-token-" + System.currentTimeMillis());
        mockData.put("username", request.getUsername());
        mockData.put("expiresIn", 3600);
        
        logger.info("Login completed successfully for username: {}", request.getUsername());
        return new AuthResponse(true, "Login successful", mockData);
    }

    private String validateRegistration(RegistrationRequest request) {
        logger.debug("Validating registration fields");
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            logger.debug("Validation failed: username is null or empty");
            return "Username is required";
        }
        
        if (!USERNAME_PATTERN.matcher(request.getUsername()).matches()) {
            logger.debug("Validation failed: username format is invalid - {}", request.getUsername());
            return "Username must be 3-20 characters and contain only letters, numbers, and underscores";
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            logger.debug("Validation failed: password is null or empty");
            return "Password is required";
        }
        
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            logger.debug("Validation failed: password format is invalid");
            return "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character";
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            logger.debug("Validation failed: email is null or empty");
            return "Email is required";
        }
        
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            logger.debug("Validation failed: email format is invalid - {}", request.getEmail());
            return "Email format is invalid";
        }
        
        if (request.getBirthday() == null) {
            logger.debug("Validation failed: birthday is null");
            return "Birthday is required";
        }
        
        LocalDate today = LocalDate.now();
        Period age = Period.between(request.getBirthday(), today);
        if (age.getYears() < 18) {
            logger.debug("Validation failed: user is under 18 years old");
            return "User must be at least 18 years old";
        }
        
        if (request.getBirthday().isAfter(today)) {
            logger.debug("Validation failed: birthday is in the future");
            return "Birthday cannot be in the future";
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhoneNumber()).matches()) {
                logger.debug("Validation failed: phone number format is invalid - {}", request.getPhoneNumber());
                return "Phone number format is invalid";
            }
        }
        
        logger.debug("All registration fields validated successfully");
        return null;
    }

    private String validateLogin(LoginRequest request) {
        logger.debug("Validating login fields");
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            logger.debug("Validation failed: username is null or empty");
            return "Username is required";
        }
        
        if (!USERNAME_PATTERN.matcher(request.getUsername()).matches()) {
            logger.debug("Validation failed: username format is invalid - {}", request.getUsername());
            return "Username must be 3-20 characters and contain only letters, numbers, and underscores";
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            logger.debug("Validation failed: password is null or empty");
            return "Password is required";
        }
        
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            logger.debug("Validation failed: password format is invalid");
            return "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character";
        }
        
        logger.debug("All login fields validated successfully");
        return null;
    }
}

