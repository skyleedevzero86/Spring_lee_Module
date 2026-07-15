package com.sleekydz86.loginstudy.userportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sleekydz86.loginstudy.userportal.service.MemberApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient.MemberApiCallResult;
import com.sleekydz86.loginstudy.userportal.web.HomeController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

@Import({UserPortalOAuth2TestConfig.class, UserPortalWebTest.MockMemberApiConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class UserPortalWebTest extends RedisTestSupport {

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
	void homeRequiresAuthentication() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/home"));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/oauth2/authorization/user-portal"));
	}

	@Test
	void homeIsAccessibleWithOidcLoginAndAuthorizedClient() throws Exception {
		// given
		var oidc = oidcLogin().idToken(token -> token
				.claim("sub", "user")
				.claim("email", "user@loginstudy.local")
				.claim("iss", "http://localhost:9000")
				.claim("aud", "user-portal"));

		// when
		var result = mockMvc.perform(get("/home").with(oidc).with(oauth2Client("user-portal")));

		// then
		result.andExpect(status().isOk())
				.andExpect(view().name("home"))
				.andExpect(model().attribute("subject", "user"))
				.andExpect(model().attributeExists("accessTokenMasked"))
				.andExpect(model().attribute("memberApiSuccess", true));
	}

	@Test
	void maskTokenHidesMiddle() {
		// given / when / then
		assertThat(HomeController.maskToken("1234567890abcdef")).isEqualTo("12345678...abcdef");
		assertThat(HomeController.maskToken("short")).isEqualTo("***");
	}

	@TestConfiguration
	static class MockMemberApiConfig {

		@Bean
		@Primary
		MemberApiClient memberApiClient() {
			MemberApiClient client = Mockito.mock(MemberApiClient.class);
			Mockito.when(client.fetchMyProfile(Mockito.anyString()))
					.thenReturn(MemberApiCallResult.success("{\"userSubject\":\"user\"}"));
			return client;
		}
	}
}
