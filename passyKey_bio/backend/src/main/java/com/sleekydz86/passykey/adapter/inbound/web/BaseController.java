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
        if (username == null || username.equals("anonymousUser")) {
            throw new IllegalStateException("사용자가 인증되어야 합니다");
        }
        return userUseCase.findByUsername(username);
    }

    protected User getUserByUsernameOrAuthenticated(String username) {
        if (username != null && !username.isEmpty()) {
            String trimmedUsername = username.trim();
            try {
                return userUseCase.findByUsername(trimmedUsername);
            } catch (com.sleekydz86.passykey.global.exception.UserNotFoundException e) {
                try {
                    return userUseCase.findByDisplayName(trimmedUsername);
                } catch (com.sleekydz86.passykey.global.exception.UserNotFoundException ex) {
                    throw new com.sleekydz86.passykey.global.exception.UserNotFoundException("사용자를 찾을 수 없습니다: " + trimmedUsername);
                }
            }
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
        String translatedMessage = translateErrorMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(translatedMessage));
    }

    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(HttpStatus status, String message) {
        String translatedMessage = translateErrorMessage(message);
        return ResponseEntity.status(status)
                .body(ApiResponse.error(translatedMessage));
    }

    protected String translateErrorMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "알 수 없는 오류";
        }
        if (message.contains("challenge") || message.contains("Challenge")) {
            return message.replace("challenge", "챌린지").replace("Challenge", "챌린지");
        }
        if (message.contains("origin") || message.contains("Origin")) {
            return message.replace("origin", "출처").replace("Origin", "출처");
        }
        if (message.contains("signature") || message.contains("Signature")) {
            return message.replace("signature", "서명").replace("Signature", "서명");
        }
        if (message.contains("credential") || message.contains("Credential")) {
            return message.replace("credential", "인증서").replace("Credential", "인증서");
        }
        if (message.contains("verification") || message.contains("Verification")) {
            return message.replace("verification", "검증").replace("Verification", "검증");
        }
        if (message.contains("validation") || message.contains("Validation")) {
            return message.replace("validation", "유효성 검사").replace("Validation", "유효성 검사");
        }
        if (message.contains("invalid") || message.contains("Invalid")) {
            return message.replace("invalid", "잘못된").replace("Invalid", "잘못된");
        }
        if (message.contains("failed") || message.contains("Failed")) {
            return message.replace("failed", "실패").replace("Failed", "실패");
        }
        if (message.contains("error") || message.contains("Error")) {
            return message.replace("error", "오류").replace("Error", "오류");
        }
        return message;
    }
}
