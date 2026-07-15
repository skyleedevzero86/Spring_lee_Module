package com.sleekydz86.loginstudy.member.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MemberApiRateLimitFilter extends OncePerRequestFilter {

	private final StringRedisTemplate redisTemplate;
	private final int limitPerMinute;

	public MemberApiRateLimitFilter(
			StringRedisTemplate redisTemplate,
			@Value("${member.rate-limit.per-minute:60}") int limitPerMinute) {
		this.redisTemplate = redisTemplate;
		this.limitPerMinute = limitPerMinute;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path == null || !path.startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String subject = resolveSubject();
		if (subject == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String key = "rate-limit:{" + subject + "}:member-api";
		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redisTemplate.expire(key, Duration.ofMinutes(1));
		}
		if (count != null && count > limitPerMinute) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
			response.getWriter().write(
					"{\"title\":\"요청 한도 초과\",\"status\":429,\"detail\":\"요청 한도를 초과했습니다. 잠시 후 다시 시도하세요.\"}");
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static String resolveSubject() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		if (authentication.getPrincipal() instanceof Jwt jwt) {
			return jwt.getSubject();
		}
		return authentication.getName();
	}
}
