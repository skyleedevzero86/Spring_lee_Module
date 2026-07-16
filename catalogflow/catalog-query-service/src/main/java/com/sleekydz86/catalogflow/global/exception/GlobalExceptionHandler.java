package com.sleekydz86.catalogflow.global.exception;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleProductNotFound(
			ProductNotFoundException exception,
			HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problemDetail.setTitle("상품을 찾을 수 없음");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/product-not-found"));
		problemDetail.setProperty("path", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgument(
			IllegalArgumentException exception,
			HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				exception.getMessage() == null ? "요청 값이 유효하지 않습니다" : exception.getMessage());
		problemDetail.setTitle("요청 검증 실패");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/validation-failed"));
		problemDetail.setProperty("path", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ProblemDetail> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"요청 파라미터 형식이 올바르지 않습니다");
		problemDetail.setTitle("요청 검증 실패");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/validation-failed"));
		problemDetail.setProperty("path", request.getRequestURI());
		problemDetail.setProperty("parameter", exception.getName());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(java.util.stream.Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() == null ? "유효하지 않은 값입니다" : error.getDefaultMessage(),
						(first, second) -> first));

		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"요청 값이 유효하지 않습니다");
		problemDetail.setTitle("요청 검증 실패");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/validation-failed"));
		problemDetail.setProperty("path", request.getRequestURI());
		problemDetail.setProperty("errors", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ProblemDetail> handleApplicationException(
			ApplicationException exception,
			HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				exception.getMessage());
		problemDetail.setTitle("애플리케이션 오류");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/application-error"));
		problemDetail.setProperty("path", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
	}
}
