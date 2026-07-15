package com.sleekydz86.loginstudy.adminportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import com.sleekydz86.loginstudy.adminportal.web.AdminHomeController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@Import({AdminPortalOAuth2TestConfig.class, AdminPortalWebTest.MockMemberApiConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class AdminPortalWebTest extends RedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void indexPageIsPublic() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/"));

		// then
		result.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	void adminRequiresAuthentication() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/admin"));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/oauth2/authorization/admin-portal"));
	}

	@Test
	void adminIsAccessibleWithRoleAdmin() throws Exception {
		// given
		var oidc = oidcLogin()
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
				.idToken(token -> token
						.claim("sub", "admin")
						.claim("email", "admin@loginstudy.local")
						.claim("roles", java.util.List.of("ADMIN"))
						.claim("iss", "http://localhost:9000")
						.claim("aud", "admin-portal"));

		// when
		var result = mockMvc.perform(get("/admin").with(oidc).with(oauth2Client("admin-portal")));

		// then
		result.andExpect(status().isOk())
				.andExpect(view().name("admin"))
				.andExpect(model().attribute("subject", "admin"))
				.andExpect(model().attributeExists("accessTokenMasked"))
				.andExpect(model().attribute("memberApiSuccess", true));
	}

	@Test
	void adminIsDeniedForRoleUser() throws Exception {
		// given
		var oidc = oidcLogin()
				.authorities(new SimpleGrantedAuthority("ROLE_USER"))
				.idToken(token -> token
						.claim("sub", "user")
						.claim("email", "user@loginstudy.local")
						.claim("roles", java.util.List.of("USER"))
						.claim("iss", "http://localhost:9000")
						.claim("aud", "admin-portal"));

		// when
		var result = mockMvc.perform(get("/admin").with(oidc).with(oauth2Client("admin-portal")));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	void maskTokenHidesMiddle() {
		// given / when / then
		assertThat(AdminHomeController.maskToken("1234567890abcdef")).isEqualTo("12345678...abcdef");
		assertThat(AdminHomeController.maskToken("short")).isEqualTo("***");
	}

	@TestConfiguration
	static class MockMemberApiConfig {

		@Bean
		@Primary
		MemberAdminApiClient memberAdminApiClient() {
			MemberAdminApiClient client = Mockito.mock(MemberAdminApiClient.class);
			Mockito.when(client.listMembers(Mockito.anyString()))
					.thenReturn(ApiCallResult.success("{\"content\":[]}"));
			return client;
		}
	}
}
