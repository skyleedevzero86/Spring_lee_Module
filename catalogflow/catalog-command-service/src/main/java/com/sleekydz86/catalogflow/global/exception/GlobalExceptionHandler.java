package com.sleekydz86.catalogflow.global.exception;

import java.net.URI;
import java.util.Map;

import com.sleekydz86.catalogflow.domain.exception.DomainException;
import com.sleekydz86.catalogflow.domain.exception.ProductVersionConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ProblemDetail> handleDomainException(DomainException exception, HttpServletRequest request) {
		HttpStatus status = exception instanceof ProductVersionConflictException
				? HttpStatus.CONFLICT
				: HttpStatus.UNPROCESSABLE_ENTITY;
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
		problemDetail.setTitle("도메인 규칙 위반");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/domain-rule-violation"));
		problemDetail.setProperty("path", request.getRequestURI());
		return ResponseEntity.status(status).body(problemDetail);
	}

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

	@ExceptionHandler(SupplierNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleSupplierNotFound(
			SupplierNotFoundException exception,
			HttpServletRequest request) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problemDetail.setTitle("공급사를 찾을 수 없음");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/supplier-not-found"));
		problemDetail.setProperty("path", request.getRequestURI());
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
}
