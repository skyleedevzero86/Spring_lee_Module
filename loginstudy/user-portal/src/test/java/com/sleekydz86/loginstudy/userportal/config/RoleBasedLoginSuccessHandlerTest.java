package com.sleekydz86.loginstudy.userportal.config;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

class RoleBasedLoginSuccessHandlerTest {

	private static final String ADMIN_LOGIN_URL =
			"http://localhost:8080/admin/oauth2/authorization/admin-portal?remember=true";

	private final RoleBasedLoginSuccessHandler handler =
			new RoleBasedLoginSuccessHandler(ADMIN_LOGIN_URL);

	@Test
	@DisplayName("관리자는 관리자 포털 로그인으로 이동한다")
	void adminMovesToAdminPortalLogin() throws Exception {
		// given
		Authentication authentication = mock(Authentication.class);
		OidcUser oidcUser = mock(OidcUser.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(authentication.getPrincipal()).thenReturn(oidcUser);
		doReturn(List.of("USER", "ADMIN")).when(oidcUser).getClaim("roles");

		// when
		handler.onAuthenticationSuccess(request, response, authentication);

		// then
		verify(response).sendRedirect(ADMIN_LOGIN_URL);
	}

	@Test
	@DisplayName("일반 사용자는 사용자 홈으로 이동한다")
	void userMovesToUserHome() throws Exception {
		// given
		Authentication authentication = mock(Authentication.class);
		OidcUser oidcUser = mock(OidcUser.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(authentication.getPrincipal()).thenReturn(oidcUser);
		doReturn(List.of("USER")).when(oidcUser).getClaim("roles");
		when(request.getContextPath()).thenReturn("/user");

		// when
		handler.onAuthenticationSuccess(request, response, authentication);

		// then
		verify(response).sendRedirect("/user/home");
	}
}
