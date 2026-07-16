package com.sleekydz86.catalogflow.global.config;

import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import com.sleekydz86.catalogflow.global.util.TraceIdHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = request.getHeader(CORRELATION_ID_HEADER);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}
		String traceId = TraceIdHolder.getOrGenerate();
		CorrelationIdHolder.set(correlationId);
		MDC.put("correlationId", correlationId);
		MDC.put("traceId", traceId);
		response.setHeader(CORRELATION_ID_HEADER, correlationId);
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			CorrelationIdHolder.clear();
			TraceIdHolder.clear();
			MDC.remove("correlationId");
			MDC.remove("traceId");
		}
	}
}
