package com.sleekydz86.toaspayment.global.exception;

import com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.sleekydz86.toaspayment.global.exception.TossPaymentException.class)
    public ResponseEntity<Map<String, Object>> handleTossPaymentException(
            com.sleekydz86.toaspayment.global.exception.TossPaymentException e) {
        log.error("토스 페이먼츠 예외 발생: {}", e.getMessage());
        return createErrorResponse(e.getStatus(), "결제 처리 오류", e.getMessage());
    }

    @ExceptionHandler(TossPaymentException.class)
    public ResponseEntity<Map<String, Object>> handleInfrastructureTossPaymentException(
            TossPaymentException e) {
        log.error("토스 페이먼츠 인프라 예외 발생: {}", e.getMessage());
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode());
        return createErrorResponse(status, "결제 처리 오류", e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 요청", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("유효성 검증 실패: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");

        return createErrorResponse(HttpStatus.BAD_REQUEST, "유효성 검증 실패", errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException e) {
        log.warn("제약 조건 위반: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "제약 조건 위반", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException e) {
        log.error("잘못된 상태 예외: {}", e.getMessage(), e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 상태", e.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException e) {
        log.warn("접근 거부: {}", e.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "접근 거부", "접근 권한이 없습니다.");
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException e) {
        log.warn("인증 실패: {}", e.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "인증 실패", "인증에 실패했습니다.");
    }

    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(
            org.springframework.web.client.RestClientException e) {
        log.error("외부 API 호출 오류: {}", e.getMessage(), e);
        return createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "외부 서비스 오류",
                "외부 결제 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Map<String, Object>> handleNumberFormatException(NumberFormatException e) {
        log.warn("숫자 형식 오류: {}", e.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 요청", "숫자 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailureException(
            org.springframework.orm.ObjectOptimisticLockingFailureException e) {
        log.warn("동시성 충돌 발생: {}", e.getMessage());
        return createErrorResponse(
                HttpStatus.CONFLICT,
                "동시성 충돌",
                "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 오류",
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}
