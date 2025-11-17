package com.sleekydz86.payment2v2.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        return headers;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        String message = e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
        log.warn("비즈니스 예외 발생: code={}, message={}", errorCode.getCode(), message);
        ErrorResponse response = ErrorResponse.of(errorCode, message);
        return ResponseEntity.status(errorCode.getStatus()).headers(createHeaders()).body(response);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("도메인 예외 발생: code={}, message={}", errorCode.getCode(), e.getDomainMessage());
        ErrorResponse response = ErrorResponse.of(errorCode, e.getDomainMessage());
        return ResponseEntity.status(errorCode.getStatus()).headers(createHeaders()).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("검증 예외 발생: code={}, errors={}", errorCode.getCode(), e.getValidationErrors());
        ErrorResponse response = ErrorResponse.of(errorCode, String.join(", ", e.getValidationErrors()));
        return ResponseEntity.status(errorCode.getStatus()).headers(createHeaders()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("입력값 검증 실패: errors={}", errors);
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .code(ErrorCode.INVALID_INPUT_VALUE.getCode())
                .message(ErrorCode.INVALID_INPUT_VALUE.getMessage())
                .detail(errors.toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(createHeaders()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 예외 발생", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(createHeaders()).body(response);
    }
}

