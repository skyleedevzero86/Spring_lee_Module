package com.sleekydz86.loginstudy.userportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.sleekydz86.loginstudy.userportal.service.AuthAccountApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient.MemberApiCallResult;
import com.sleekydz86.loginstudy.userportal.web.HomeController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
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

@Import({
	UserPortalOAuth2TestConfig.class,
	UserPortalWebTest.MockMemberApiConfig.class,
	UserPortalWebTest.MockAuthApiConfig.class
})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class UserPortalWebTest extends RedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("인덱스 페이지는 공개된다")
	void indexPageIsPublic() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/"));

		// then
		result.andExpect(status().isOk())
				.andExpect(view().name("index"));
	}

	@Test
	@DisplayName("로그인 유지 쿠키가 있으면 기존 인증 세션을 사용하는 로그인 링크를 제공한다")
	void indexUsesRememberLoginWhenPreferenceCookieExists() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/")
				.cookie(new Cookie("LOGIN_REMEMBER", "true")));

		// then
		result.andExpect(status().isOk())
				.andExpect(model().attribute("rememberLogin", true))
				.andExpect(xpath("//a[contains(@href, 'remember=true')]").exists());
	}

	@Test
	@DisplayName("홈 페이지는 인증이 필요하다")
	void homeRequiresAuthentication() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/home"));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/oauth2/authorization/user-portal"));
	}

	@Test
	@DisplayName("OIDC 로그인과 인가된 클라이언트로 홈에 접근할 수 있다")
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
				.andExpect(model().attributeExists("profile"))
				.andExpect(model().attribute("memberApiSuccess", true));
	}

	@Test
	@DisplayName("로그인 사용자는 자신의 표시 이름을 변경할 수 있다")
	void authenticatedUserCanUpdateDisplayName() throws Exception {
		// given
		var oidc = oidcLogin().idToken(token -> token
				.claim("sub", "user")
				.claim("email", "user@loginstudy.local")
				.claim("aud", "user-portal"));

		// when
		var result = mockMvc.perform(post("/profile")
				.param("displayName", "새 이름")
				.param("email", "new@example.com")
				.param("phone", "010-1234-5678")
				.with(oidc)
				.with(oauth2Client("user-portal"))
				.with(csrf()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home?updated=1"));
	}

	@Test
	@DisplayName("토큰 마스킹은 중간 값을 숨긴다")
	void maskTokenHidesMiddle() {
		// given / when / then
		assertThat(HomeController.maskToken("1234567890abcdef")).isEqualTo("12345678...abcdef");
		assertThat(HomeController.maskToken("short")).isEqualTo("***");
	}

	@TestConfiguration
	static class MockAuthApiConfig {

		@Bean
		@Primary
		AuthAccountApiClient authAccountApiClient() {
			AuthAccountApiClient client = Mockito.mock(AuthAccountApiClient.class);
			Mockito.when(client.getOwnProfile(Mockito.anyString()))
					.thenReturn(AuthAccountApiClient.ApiCallResult.success(
							"{\"id\":1,\"username\":\"user\",\"displayName\":\"Demo User\","
									+ "\"email\":\"user@example.com\",\"phone\":\"010-0000-0000\","
									+ "\"status\":\"ACTIVE\",\"roles\":[\"USER\"]}"));
			Mockito.when(client.updateOwnProfile(
							Mockito.anyString(),
							Mockito.eq("새 이름"),
							Mockito.eq("new@example.com"),
							Mockito.eq("010-1234-5678")))
					.thenReturn(AuthAccountApiClient.ApiCallResult.success("{}"));
			return client;
		}
	}

	@TestConfiguration
	static class MockMemberApiConfig {

		@Bean
		@Primary
		MemberApiClient memberApiClient() {
			MemberApiClient client = Mockito.mock(MemberApiClient.class);
			Mockito.when(client.fetchMyProfile(Mockito.anyString()))
					.thenReturn(MemberApiCallResult.success(
							"{\"id\":1,\"userSubject\":\"user\",\"displayName\":\"Demo User\",\"version\":0}"));
			Mockito.when(client.updateProfile(
							Mockito.anyString(),
							Mockito.eq(1L),
							Mockito.contains("새 이름")))
					.thenReturn(MemberApiCallResult.success(
							"{\"id\":1,\"userSubject\":\"user\",\"displayName\":\"새 이름\",\"version\":1}"));
			return client;
		}
	}
}
