package com.sleekydz86.catalogflow.global.exception;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ProblemDetail> handleApplicationException(
			ApplicationException exception,
			HttpServletRequest request) {
		HttpStatus status = exception.getMessage() != null && exception.getMessage().contains("찾을 수 없습니다")
				? HttpStatus.NOT_FOUND
				: HttpStatus.BAD_REQUEST;
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
		problemDetail.setTitle("배치 요청 처리 실패");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/batch-request-failed"));
		problemDetail.setProperty("path", request.getRequestURI());
		return ResponseEntity.status(status).body(problemDetail);
	}
}
