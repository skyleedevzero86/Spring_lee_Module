package com.sleekydz86.loginstudy.userportal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	void logoutWithoutCsrfTokenIsForbidden() throws Exception {
		// given
		var authenticated = oidcLogin().idToken(token -> token.claim("sub", "user"));

		// when
		var result = mockMvc.perform(post("/logout").with(authenticated));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	void logoutWithCsrfTokenSucceeds() throws Exception {
		// given
		var authenticated = oidcLogin().idToken(token -> token.claim("sub", "user"));

		// when
		var result = mockMvc.perform(post("/logout").with(authenticated).with(csrf()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));
	}
}
