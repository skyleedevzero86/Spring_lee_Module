package com.sleekydz86.passykey.adapter.inbound.web;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final UserUseCase userUseCase;

    protected BaseController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    protected User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("사용자가 인증되어야 합니다");
        }
        String username = authentication.getName();
        return userUseCase.findByUsername(username);
    }

    protected User getUserByUsernameOrAuthenticated(String username) {
        if (username != null && !username.isEmpty()) {
            return userUseCase.findByUsername(username);
        }
        return getAuthenticatedUser();
    }

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> successResponse(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> createdResponse(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message));
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message));
    }
}




