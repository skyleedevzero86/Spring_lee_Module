package com.sleekydz86.loginstudy.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class LoginRememberSuccessHandler implements AuthenticationSuccessHandler {

	public static final String COOKIE_NAME = "LOGIN_REMEMBER";

	private final AuthenticationSuccessHandler delegate =
			new SavedRequestAwareAuthenticationSuccessHandler();

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		boolean rememberLogin = "true".equals(request.getParameter("rememberLogin"));
		ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, Boolean.toString(rememberLogin))
				.httpOnly(true)
				.secure(request.isSecure())
				.sameSite("Lax")
				.path("/")
				.maxAge(rememberLogin ? Duration.ofDays(30) : Duration.ZERO)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		delegate.onAuthenticationSuccess(request, response, authentication);
	}
}
