package com.sleekydz86.opspilot.global.web;

import com.sleekydz86.opspilot.fault.domain.SimulatedServerException;
import com.sleekydz86.opspilot.global.exception.DomainException;
import com.sleekydz86.opspilot.global.exception.ExternalServiceException;
import com.sleekydz86.opspilot.global.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public final class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(SimulatedServerException.class)
	public ResponseEntity<Map<String, String>> handleSimulated(SimulatedServerException ex) {
		log.warn("시뮬레이션 장애: {} - {}", ex.code(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of("code", ex.code(), "message", ex.getMessage()));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다", ex.getMessage());
	}

	@ExceptionHandler(ExternalServiceException.class)
	public ProblemDetail handleExternal(ExternalServiceException ex) {
		log.error("외부 서비스 오류: {}", ex.getMessage(), ex);
		return problem(HttpStatus.BAD_GATEWAY, "외부 AI 서비스 오류", ex.getMessage());
	}

	@ExceptionHandler({DomainException.class, IllegalArgumentException.class})
	public ProblemDetail handleDomain(RuntimeException ex) {
		return problem(HttpStatus.BAD_REQUEST, "잘못된 요청입니다", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		String detail = ex.getBindingResult().getFieldErrors().stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.reduce((a, b) -> a + ", " + b)
			.orElse("입력값이 올바르지 않습니다");
		return problem(HttpStatus.BAD_REQUEST, "입력값 검증 실패", detail);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("예상치 못한 오류가 발생했습니다", ex);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류", "요청 처리 중 오류가 발생했습니다");
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now().toString());
		return problem;
	}
}
