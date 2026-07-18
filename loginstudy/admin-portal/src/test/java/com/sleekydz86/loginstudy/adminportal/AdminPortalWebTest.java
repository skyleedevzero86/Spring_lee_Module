package com.sleekydz86.loginstudy.adminportal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sleekydz86.loginstudy.adminportal.service.AuthAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import com.sleekydz86.loginstudy.adminportal.web.AdminHomeController;
import com.sleekydz86.loginstudy.adminportal.web.AdminMemberView;
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

@Import({
		AdminPortalOAuth2TestConfig.class,
		AdminPortalWebTest.MockMemberApiConfig.class,
		AdminPortalWebTest.MockAuthApiConfig.class
})
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
				.andExpect(content().string(containsString(
						"data-url=\"/admin/members/1/sensitive/DISPLAY_NAME/reveal\"")))
				.andExpect(content().string(containsString("홍*동")))
				.andExpect(content().string(containsString("value=\"010-9999-8888\"")))
				.andExpect(content().string(containsString("활성")));
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

	@Test
	@DisplayName("인증 사용자 조회가 없어도 회원 상태를 표시한다")
	void memberStatusIsFallbackWhenAccountStatusIsUnavailable() {
		AdminMemberView.Member member = new AdminMemberView.Member(
				1L,
				"user",
				"u***@example.com",
				"홍*동",
				"SUSPENDED",
				null,
				null,
				null);

		assertThat(member.effectiveStatus()).isEqualTo("SUSPENDED");
	}

	@Test
	@DisplayName("관리자는 회원의 권한을 변경할 수 있다")
	void adminCanChangeMemberRole() throws Exception {
		// given
		var oidc = oidcLogin()
				.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
				.idToken(token -> token.claim("sub", "admin"));

		// when
		var result = mockMvc.perform(post("/admin/members/1/role")
				.param("username", "user")
				.param("role", "ADMIN")
				.with(oidc)
				.with(oauth2Client("admin-portal"))
				.with(csrf()));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin?accountUpdated=1"));
	}

	@TestConfiguration
	static class MockAuthApiConfig {

		@Bean
		@Primary
		AuthAdminApiClient authAdminApiClient() {
			AuthAdminApiClient client = Mockito.mock(AuthAdminApiClient.class);
			Mockito.when(client.listUsers(Mockito.anyString()))
					.thenReturn(AuthAdminApiClient.ApiCallResult.success("""
							[{"id":1,"username":"user","email":"user@example.com",
							"displayName":"홍길동","phone":"010-1234-5678","status":"ACTIVE",
							"enabled":true,"accountNonLocked":true,"roles":["USER"]}]
							"""));
			Mockito.when(client.getOwnProfile(Mockito.anyString()))
					.thenReturn(AuthAdminApiClient.ApiCallResult.success("""
							{"id":2,"username":"admin","email":"admin@loginstudy.local",
							"displayName":"admin","phone":"010-9999-8888","status":"ACTIVE",
							"enabled":true,"accountNonLocked":true,"roles":["ADMIN"]}
							"""));
			Mockito.when(client.changeRole(
					Mockito.anyString(), Mockito.eq("user"), Mockito.eq("ADMIN")))
					.thenReturn(AuthAdminApiClient.ApiCallResult.success("{}"));
			return client;
		}
	}

	@TestConfiguration
	static class MockMemberApiConfig {

		@Bean
		@Primary
		MemberAdminApiClient memberAdminApiClient() {
			MemberAdminApiClient client = Mockito.mock(MemberAdminApiClient.class);
			Mockito.when(client.listMembers(Mockito.anyString()))
					.thenReturn(ApiCallResult.success("""
							{"content":[{"id":1,"userSubject":"user",
							"email":"a***@loginstudy.local","displayName":"홍*동",
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
