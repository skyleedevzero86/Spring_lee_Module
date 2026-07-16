package com.sleekydz86.catalogflow.security;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

public class CatalogSecurityProblemHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public CatalogSecurityProblemHandlers(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		writeProblem(
				response,
				HttpStatus.UNAUTHORIZED,
				"인증이 필요합니다",
				"요청에 유효한 액세스 토큰이 없습니다",
				"https://catalogflow.local/problems/unauthorized",
				request.getRequestURI());
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		writeProblem(
				response,
				HttpStatus.FORBIDDEN,
				"접근이 거부되었습니다",
				"이 작업을 수행할 권한이 없습니다",
				"https://catalogflow.local/problems/forbidden",
				request.getRequestURI());
	}

	private void writeProblem(
			HttpServletResponse response,
			HttpStatus status,
			String title,
			String detail,
			String type,
			String path) throws IOException {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(title);
		problemDetail.setType(URI.create(type));
		problemDetail.setProperty("path", path);
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problemDetail);
	}
}
