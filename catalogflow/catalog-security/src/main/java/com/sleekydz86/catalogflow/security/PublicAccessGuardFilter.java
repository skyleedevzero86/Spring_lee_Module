package com.sleekydz86.catalogflow.security;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class PublicAccessGuardFilter extends OncePerRequestFilter {

	private final CatalogNetworkProperties networkProperties;
	private final ObjectMapper objectMapper;

	public PublicAccessGuardFilter(CatalogNetworkProperties networkProperties, ObjectMapper objectMapper) {
		this.networkProperties = networkProperties;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (networkProperties.isPublicAccessEnabled()) {
			filterChain.doFilter(request, response);
			return;
		}
		String path = request.getRequestURI();
		if (networkProperties.isActuatorAccessEnabled() && path != null && path.startsWith("/actuator")) {
			filterChain.doFilter(request, response);
			return;
		}
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.FORBIDDEN,
				"이 서비스는 외부에서 직접 접근할 수 없습니다. 메인 서비스(%s:%d)를 이용하세요."
						.formatted(networkProperties.getMainServiceName(), networkProperties.getMainServicePort()));
		problemDetail.setTitle("외부 직접 접근이 거부되었습니다");
		problemDetail.setType(URI.create("https://catalogflow.local/problems/public-access-denied"));
		problemDetail.setProperty("path", path);
		problemDetail.setProperty("mainServicePort", networkProperties.getMainServicePort());
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), problemDetail);
	}
}
