package com.sleekydz86.loginstudy.adminportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import com.sleekydz86.loginstudy.adminportal.web.AdminHomeController;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@Import({AdminPortalOAuth2TestConfig.class, AdminPortalWebTest.MockMemberApiConfig.class})
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class AdminPortalWebTest extends RedisTestSupport {

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
	@DisplayName("관리자 페이지는 인증이 필요하다")
	void adminRequiresAuthentication() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/admin"));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/oauth2/authorization/admin-portal"));
	}

	@Test
	@DisplayName("관리자 역할로 관리자 페이지에 접근할 수 있다")
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
				.andExpect(model().attributeExists("members"))
				.andExpect(model().attribute("memberApiSuccess", true))
				.andExpect(xpath("//button[@data-url='/admin/members/1/sensitive/DISPLAY_NAME/reveal']")
						.string("홍*동"));
	}

	@Test
	@DisplayName("관리자는 CSRF 보호된 요청으로 선택한 민감정보만 복원한다")
	void adminCanRevealOnlySelectedSensitiveField() throws Exception {
		// given
		var oidc = oidcLogin()
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
				.idToken(token -> token
						.claim("sub", "admin")
						.claim("roles", java.util.List.of("ADMIN")));

		// when
		var result = mockMvc.perform(post("/admin/members/1/sensitive/DISPLAY_NAME/reveal")
				.with(oidc)
				.with(oauth2Client("admin-portal"))
				.with(csrf()));

		// then
		result.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", "no-store"))
				.andExpect(jsonPath("$.value").value("홍길동"));
	}

	@Test
	@DisplayName("사용자 역할은 관리자 페이지 접근이 거부된다")
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
	@DisplayName("토큰 마스킹은 중간 값을 숨긴다")
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
					.thenReturn(ApiCallResult.success("""
							{"content":[{"id":1,"email":"a***@loginstudy.local","displayName":"홍*동",
							"status":"ACTIVE","joinedAt":"2026-07-17T00:00:00Z"}],
							"page":0,"size":20,"totalElements":1,"totalPages":1}
							"""));
			Mockito.when(client.revealSensitiveField(
							Mockito.anyString(), Mockito.eq(1L), Mockito.eq("DISPLAY_NAME")))
					.thenReturn(ApiCallResult.success(
							"{\"memberId\":1,\"field\":\"DISPLAY_NAME\",\"value\":\"홍길동\"}"));
			return client;
		}
	}
}
