package com.sleekydz86.toaspayment.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.sleekydz86.toaspayment.global.exception.BadRequestException;
import com.sleekydz86.toaspayment.global.exception.GlobalExceptionHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Global Exception Handler 테스트")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("BadRequestException 처리")
    void handleBadRequestException() {

        BadRequestException exception = new BadRequestException("잘못된 요청입니다");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBadRequestException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("잘못된 요청");
        assertThat(response.getBody().get("message")).isEqualTo("잘못된 요청입니다");
    }

    @Test
    @DisplayName("TossPaymentException 처리")
    void handleTossPaymentException() {

        com.sleekydz86.toaspayment.global.exception.TossPaymentException exception = new com.sleekydz86.toaspayment.global.exception.TossPaymentException(
                "결제 처리 실패",
                HttpStatus.BAD_REQUEST);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleTossPaymentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("결제 처리 오류");
        assertThat(response.getBody().get("message")).isEqualTo("결제 처리 실패");
    }

    @Test
    @DisplayName("Infrastructure TossPaymentException 처리")
    void handleInfrastructureTossPaymentException() {

        com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException exception = new com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException(
                "결제 승인 실패", 400);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleInfrastructureTossPaymentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("결제 처리 오류");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리")
    void handleValidationException() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "email", "이메일은 필수 입력값입니다.");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("유효성 검증 실패");
    }

    @Test
    @DisplayName("ConstraintViolationException 처리")
    void handleConstraintViolationException() {

        ConstraintViolationException exception = new ConstraintViolationException("제약 조건 위반", new HashSet<>());

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler
                .handleConstraintViolationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("제약 조건 위반");
    }

    @Test
    @DisplayName("IllegalStateException 처리")
    void handleIllegalStateException() {

        IllegalStateException exception = new IllegalStateException("잘못된 상태입니다");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalStateException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("잘못된 상태");
    }

    @Test
    @DisplayName("AccessDeniedException 처리")
    void handleAccessDeniedException() {

        AccessDeniedException exception = new AccessDeniedException("접근 거부");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleAccessDeniedException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(403);
        assertThat(response.getBody().get("error")).isEqualTo("접근 거부");
    }

    @Test
    @DisplayName("BadCredentialsException 처리")
    void handleBadCredentialsException() {

        BadCredentialsException exception = new BadCredentialsException("인증 실패");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBadCredentialsException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(401);
        assertThat(response.getBody().get("error")).isEqualTo("인증 실패");
    }

    @Test
    @DisplayName("일반 Exception 처리")
    void handleException() {

        Exception exception = new Exception("예상치 못한 오류");

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("서버 오류");
    }
}
