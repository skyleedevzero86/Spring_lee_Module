package com.sleekydz86.passykey.global.exception;

import com.sleekydz86.passykey.application.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("유효성 검증 실패"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("잘못된 인자 예외 발생", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        logger.error("인증 예외 발생", ex);
        String message = ex.getMessage();
        if (message != null && !message.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("인증 실패: " + translateErrorMessage(message)));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("인증 실패"));
    }

    @ExceptionHandler(WebAuthnException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebAuthnException(WebAuthnException ex) {
        logger.error("WebAuthn 예외 발생", ex);
        String message = ex.getMessage();
        if (message != null && (message.contains("인증 실패") || message.contains("인증서") || message.contains("챌린지"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredentialNotFoundException(CredentialNotFoundException ex) {
        logger.error("인증서를 찾을 수 없음", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ChallengeExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleChallengeExpiredException(ChallengeExpiredException ex) {
        logger.error("챌린지 만료", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCounterException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCounterException(InvalidCounterException ex) {
        logger.error("잘못된 카운터 값", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("사용자를 찾을 수 없음", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        logger.error("잘못된 상태 예외 발생", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        if (logger.isDebugEnabled()) {
            logger.debug("리소스를 찾을 수 없음: {}", ex.getResourcePath());
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        logger.error("런타임 예외 발생", ex);
        String message = ex.getMessage();
        if (message != null && (message.contains("등록 검증 실패") || message.contains("인증 검증 실패") ||
                message.contains("사용자 저장 실패") || message.contains("사용자 조회 실패") ||
                message.contains("Origin 추출 실패") || message.contains("clientDataJSON"))) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(message));
        }
        if (message != null && !message.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("오류가 발생했습니다: " + translateErrorMessage(message)));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("오류가 발생했습니다"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("예상치 못한 예외 발생", ex);
        String message = ex.getMessage();
        if (message != null && !message.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("예상치 못한 오류가 발생했습니다: " + translateErrorMessage(message)));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("예상치 못한 오류가 발생했습니다"));
    }

    private String translateErrorMessage(String message) {
        if (message == null) {
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
