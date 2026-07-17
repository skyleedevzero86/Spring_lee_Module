package com.sleekydz86.loginstudy.userportal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(UserPortalOAuth2TestConfig.class)
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class UserPortalSecurityHardeningTest extends RedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("CSRF 토큰 없는 로그아웃은 금지된다")
	void logoutWithoutCsrfTokenIsForbidden() throws Exception {
		// given
		var authenticated = oidcLogin().idToken(token -> token.claim("sub", "user"));

		// when
		var result = mockMvc.perform(post("/logout").with(authenticated));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("CSRF 토큰이 있는 로그아웃은 성공한다")
	void logoutWithCsrfTokenSucceeds() throws Exception {
		// given
		var authenticated = oidcLogin().idToken(token -> token.claim("sub", "user"));

		// when
		var result = mockMvc.perform(post("/logout").with(authenticated).with(csrf()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));
	}

	@Test
	@DisplayName("로그인 유지 없이 로그인하면 자격 증명 입력을 강제한다")
	void loginWithoutRememberForcesCredentialPrompt() throws Exception {
		// when / then
		mockMvc.perform(get("/oauth2/authorization/user-portal"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", containsString("prompt=login")));
	}

	@Test
	@DisplayName("로그인 유지로 로그인하면 통합 로그인을 유지한다")
	void loginWithRememberKeepsSingleSignOn() throws Exception {
		// when / then
		mockMvc.perform(get("/oauth2/authorization/user-portal")
						.param("remember", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", not(containsString("prompt=login"))));
	}
}
