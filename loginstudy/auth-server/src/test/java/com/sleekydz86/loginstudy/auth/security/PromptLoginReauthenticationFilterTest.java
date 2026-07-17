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
}
