package com.sleekydz86.loginstudy.auth.security;

import com.sleekydz86.loginstudy.auth.domain.LoginHistory;
import com.sleekydz86.loginstudy.auth.repository.LoginHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class LoginHistoryEventListener {

	private final LoginHistoryRepository loginHistoryRepository;
	private final LoginProtectionService loginProtectionService;

	public LoginHistoryEventListener(
			LoginHistoryRepository loginHistoryRepository,
			LoginProtectionService loginProtectionService) {
		this.loginHistoryRepository = loginHistoryRepository;
		this.loginProtectionService = loginProtectionService;
	}

	@EventListener
	public void onSuccess(AuthenticationSuccessEvent event) {
		Authentication authentication = event.getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return;
		}
		if (isClientAuthentication(authentication)) {
			return;
		}
		loginProtectionService.onSuccess(authentication.getName());
		HttpServletRequest request = currentRequest();
		loginHistoryRepository.save(new LoginHistory(
				authentication.getName(),
				true,
				clientIp(request),
				userAgent(request),
				null));
	}

	@EventListener
	public void onFailure(AbstractAuthenticationFailureEvent event) {
		Authentication authentication = event.getAuthentication();
		String username = authentication != null ? authentication.getName() : "unknown";
		loginProtectionService.onFailure(username);
		HttpServletRequest request = currentRequest();
		String reason = event.getException() != null ? event.getException().getClass().getSimpleName() : "unknown";
		loginHistoryRepository.save(new LoginHistory(
				username,
				false,
				clientIp(request),
				userAgent(request),
				reason));
	}

	private static boolean isClientAuthentication(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_CLIENT".equals(authority.getAuthority()));
	}

	private static HttpServletRequest currentRequest() {
		if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
			return attributes.getRequest();
		}
		return null;
	}

	private static String clientIp(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static String userAgent(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null) {
			return null;
		}
		return userAgent.length() > 512 ? userAgent.substring(0, 512) : userAgent;
	}
}
