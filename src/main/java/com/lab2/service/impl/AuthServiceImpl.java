package com.lab2.service.impl;

import com.lab2.dto.AuthResponse;
import com.lab2.dto.LoginRequest;
import com.lab2.dto.RegistrationRequest;
import com.lab2.service.AuthService;
import com.lab2.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private final TokenService tokenService;

    @Autowired
    public AuthServiceImpl(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AuthResponse register(RegistrationRequest request) {
        logger.info("Початок процесу реєстрації для користувача: {}", request.getUsername());
        
        String validationError = validateRegistration(request);
        if (validationError != null) {
            logger.warn("Помилка валідації при реєстрації: {}", validationError);
            return new AuthResponse(false, validationError, null);
        }
        
        logger.info("Валідація реєстрації успішна для користувача: {}", request.getUsername());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", 12345L);
        metadata.put("registeredAt", LocalDate.now().toString());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            metadata.put("phoneNumber", request.getPhoneNumber());
        }
        
        String token = tokenService.generateToken(request.getUsername(), request.getEmail(), metadata);
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("userId", 12345L);
        mockData.put("username", request.getUsername());
        mockData.put("email", request.getEmail());
        mockData.put("registeredAt", LocalDate.now().toString());
        mockData.put("token", token);
        
        logger.info("Реєстрація успішно завершена для користувача: {}. Токен згенеровано", request.getUsername());
        return new AuthResponse(true, "Користувача успішно зареєстровано", mockData);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Початок процесу авторизації для користувача: {}", request.getUsername());
        
        String validationError = validateLogin(request);
        if (validationError != null) {
            logger.warn("Помилка валідації при авторизації: {}", validationError);
            return new AuthResponse(false, validationError, null);
        }
        
        logger.info("Валідація авторизації успішна для користувача: {}", request.getUsername());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("lastLogin", LocalDate.now().toString());
        
        String token = tokenService.generateToken(request.getUsername(), "user@example.com", metadata);
        
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("token", token);
        mockData.put("username", request.getUsername());
        mockData.put("expiresIn", 86400);
        
        logger.info("Авторизація успішно завершена для користувача: {}. Токен згенеровано", request.getUsername());
        return new AuthResponse(true, "Авторизація успішна", mockData);
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

