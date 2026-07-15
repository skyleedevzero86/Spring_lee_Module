package com.sleekydz86.loginstudy.auth.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenEndpointMetricsFilter extends OncePerRequestFilter {

	private final AuthSecurityMetrics authSecurityMetrics;

	public TokenEndpointMetricsFilter(AuthSecurityMetrics authSecurityMetrics) {
		this.authSecurityMetrics = authSecurityMetrics;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !"/oauth2/token".equals(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		filterChain.doFilter(request, response);
		int status = response.getStatus();
		if (status >= 200 && status < 300) {
			authSecurityMetrics.incrementTokenIssued();
		}
		else if (status >= 400) {
			authSecurityMetrics.incrementTokenIssueFailure();
		}
	}
}
