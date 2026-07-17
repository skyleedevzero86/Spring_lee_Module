package com.sleekydz86.loginstudy.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

public class PromptLoginReauthenticationFilter extends OncePerRequestFilter {

	private static final String REAUTHENTICATION_STATE =
			PromptLoginReauthenticationFilter.class.getName() + ".STATE";

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String prompt = request.getParameter("prompt");
		return !"/oauth2/authorize".equals(request.getServletPath())
				|| prompt == null
				|| Arrays.stream(prompt.split("\\s+")).noneMatch("login"::equals);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String state = request.getParameter("state");
		HttpSession session = request.getSession(false);
		if (session != null && state != null && state.equals(session.getAttribute(REAUTHENTICATION_STATE))) {
			session.removeAttribute(REAUTHENTICATION_STATE);
			filterChain.doFilter(request, response);
			return;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			if (state != null) {
				request.getSession(true).setAttribute(REAUTHENTICATION_STATE, state);
			}
		}
		filterChain.doFilter(request, response);
	}
}
