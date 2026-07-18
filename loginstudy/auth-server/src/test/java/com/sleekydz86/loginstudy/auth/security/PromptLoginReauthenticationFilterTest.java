package com.sleekydz86.loginstudy.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class PromptLoginReauthenticationFilterTest {

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("prompt login 요청은 기존 인증을 제거하고 재인증을 요구한다")
	void promptLoginClearsExistingAuthentication() throws Exception {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
		request.setServletPath("/oauth2/authorize");
		request.setParameter("prompt", "login");
		request.setParameter("state", "state-123");
		request.getSession(true);
		MockHttpServletResponse response = new MockHttpServletResponse();
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						"admin",
						"password",
						List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
		AtomicBoolean chainCalled = new AtomicBoolean();

		// when
		new PromptLoginReauthenticationFilter().doFilter(
				request,
				response,
				(servletRequest, servletResponse) -> {
					chainCalled.set(true);
					assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
				});

		// then
		assertThat(chainCalled).isTrue();
		assertThat(request.getSession(false)).isNotNull();
	}

	@Test
	@DisplayName("첫 로그인 후 같은 OAuth 요청으로 복귀하면 인증을 유지한다")
	void promptLoginKeepsAuthenticationAfterInitialLogin() throws Exception {
		// given
		PromptLoginReauthenticationFilter filter = new PromptLoginReauthenticationFilter();
		MockHttpServletRequest initialRequest = promptLoginRequest("state-456");
		MockHttpServletResponse initialResponse = new MockHttpServletResponse();

		// when
		filter.doFilter(initialRequest, initialResponse, (request, response) -> {
		});
		var session = initialRequest.getSession(false);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(
						"user",
						"password",
						List.of(new SimpleGrantedAuthority("ROLE_USER"))));
		MockHttpServletRequest resumedRequest = promptLoginRequest("state-456");
		resumedRequest.setSession(session);
		AtomicBoolean authenticationKept = new AtomicBoolean();
		filter.doFilter(
				resumedRequest,
				new MockHttpServletResponse(),
				(request, response) -> authenticationKept.set(
						SecurityContextHolder.getContext().getAuthentication() != null));

		// then
		assertThat(authenticationKept).isTrue();
	}

	private static MockHttpServletRequest promptLoginRequest(String state) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth2/authorize");
		request.setServletPath("/oauth2/authorize");
		request.setParameter("prompt", "login");
		request.setParameter("state", state);
		return request;
	}
}
