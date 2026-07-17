package com.sleekydz86.loginstudy.userportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final String adminLoginUrl;

	public RoleBasedLoginSuccessHandler(
			@Value("${loginstudy.portal.admin-login-url:"
					+ "http://localhost:8082/oauth2/authorization/admin-portal?remember=true}")
			String adminLoginUrl) {
		this.adminLoginUrl = adminLoginUrl;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (isAdmin(authentication)) {
			response.sendRedirect(this.adminLoginUrl);
			return;
		}
		response.sendRedirect(request.getContextPath() + "/home");
	}

	static boolean isAdmin(Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
			return false;
		}
		Collection<?> roles = oidcUser.getClaim("roles");
		return roles != null && roles.stream()
				.map(String::valueOf)
				.anyMatch(role -> role.equals("ADMIN") || role.equals("ROLE_ADMIN"));
	}
}
