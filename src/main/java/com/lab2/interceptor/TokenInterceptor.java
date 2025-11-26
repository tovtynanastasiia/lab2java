package com.lab2.interceptor;

import com.lab2.annotation.RequiresToken;
import com.lab2.model.TokenData;
import com.lab2.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_ATTRIBUTE = "tokenData";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String EMAIL_ATTRIBUTE = "email";
    
    private final TokenService tokenService;

    @Autowired
    public TokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresToken requiresToken = handlerMethod.getMethodAnnotation(RequiresToken.class);
        
        if (requiresToken == null) {
            logger.debug("Ендпоінт {} не вимагає токену", request.getRequestURI());
            return true;
        }
        
        logger.info("Перевірка токену для ендпоінту: {}", request.getRequestURI());
        
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            logger.warn("Відсутній або некоректний заголовок Authorization для ендпоінту: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Необхідна авторизація\",\"message\":\"Токен відсутній або некоректний\"}");
            return false;
        }
        
        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
            TokenData tokenData = tokenService.validateToken(token);
            
            request.setAttribute(TOKEN_ATTRIBUTE, tokenData);
            request.setAttribute(USERNAME_ATTRIBUTE, tokenData.getUsername());
            request.setAttribute(EMAIL_ATTRIBUTE, tokenData.getEmail());
            
            logger.info("Токен успішно перевірено. Користувач: {}, Ендпоінт: {}", 
                       tokenData.getUsername(), request.getRequestURI());
            return true;
            
        } catch (IllegalStateException e) {
            logger.warn("Помилка перевірки токену для ендпоінту {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Невалідний токен\",\"message\":\"" + e.getMessage() + "\"}");
            return false;
        } catch (Exception e) {
            logger.error("Невідома помилка при перевірці токену: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Внутрішня помилка сервера\",\"message\":\"Помилка обробки токену\"}");
            return false;
        }
    }
}

