package com.sleekydz86.loginstudy.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

class LoginRememberSuccessHandlerTest {

	@Test
	@DisplayName("로그인 상태 유지를 선택하면 자동 로그인 쿠키를 30일간 저장한다")
	void checkedRememberLoginStoresPreferenceCookie() throws Exception {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
		request.setParameter("rememberLogin", "true");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// when
		new LoginRememberSuccessHandler().onAuthenticationSuccess(
				request,
				response,
				mock(Authentication.class));

		// then
		String cookie = response.getHeader(HttpHeaders.SET_COOKIE);
		assertThat(cookie)
				.contains("LOGIN_REMEMBER=true")
				.contains("Max-Age=2592000")
				.contains("Path=/")
				.contains("HttpOnly")
				.contains("SameSite=Lax");
	}

	@Test
	@DisplayName("로그인 상태 유지를 선택하지 않으면 자동 로그인 쿠키를 제거한다")
	void uncheckedRememberLoginDeletesPreferenceCookie() throws Exception {
		// given
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// when
		new LoginRememberSuccessHandler().onAuthenticationSuccess(
				request,
				response,
				mock(Authentication.class));

		// then
		assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
				.contains("LOGIN_REMEMBER=false")
				.contains("Max-Age=0");
	}
}
