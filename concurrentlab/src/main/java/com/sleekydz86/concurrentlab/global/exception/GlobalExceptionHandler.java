package com.sleekydz86.concurrentlab.global.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(LookupFailedException.class)
	public ResponseEntity<Map<String, Object>> handleLookupFailed(LookupFailedException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "요청 처리 중 오류가 발생했습니다"));
	}

	private static Map<String, Object> errorBody(HttpStatus status, String message) {
		return Map.of(
			"status", status.value(),
			"message", message,
			"timestamp", Instant.now().toString()
		);
	}
}
