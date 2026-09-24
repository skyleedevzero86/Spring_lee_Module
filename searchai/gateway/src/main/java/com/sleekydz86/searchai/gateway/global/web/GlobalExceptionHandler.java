package com.sleekydz86.searchai.gateway.global.web;

import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public final class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(StatusRuntimeException.class)
	public ProblemDetail handleGrpc(StatusRuntimeException ex) {
		log.error("gRPC 호출 실패: {}", ex.getStatus(), ex);
		HttpStatus status = switch (ex.getStatus().getCode()) {
			case NOT_FOUND -> HttpStatus.NOT_FOUND;
			case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
			case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
			case UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
			default -> HttpStatus.BAD_GATEWAY;
		};
		return problem(status, "내부 AI 서비스 오류", "AI 서비스 호출에 실패했습니다: " + ex.getStatus().getCode());
	}

	@ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
	public ProblemDetail handleStatus(org.springframework.web.server.ResponseStatusException ex) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		return problem(status, status.getReasonPhrase(), ex.getReason() == null ? "요청을 처리할 수 없습니다" : ex.getReason());
	}

	@ExceptionHandler({IllegalArgumentException.class})
	public ProblemDetail handleDomain(RuntimeException ex) {
		return problem(HttpStatus.BAD_REQUEST, "잘못된 요청입니다", ex.getMessage());
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public ProblemDetail handleValidation(WebExchangeBindException ex) {
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
