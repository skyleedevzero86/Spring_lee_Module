package com.sleekydz86.loginstudy.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class MemberResourceServerAuthorizationTest extends MemberRedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberProfileRepository memberProfileRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void userCanReadOwnProfileWithMemberReadScope() throws Exception {
		// given
		var jwt = userJwt("user", List.of("USER"), "openid member.read");

		// when
		var result = mockMvc.perform(get("/api/members/me").with(jwt));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.userSubject").value("user"))
				.andExpect(jsonPath("$.email").value("user@loginstudy.local"));
	}

	@Test
	void requestWithoutTokenIsUnauthorized() throws Exception {
		// given / when
		var result = mockMvc.perform(get("/api/members/me"));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@Test
	void userWithoutMemberReadScopeIsForbidden() throws Exception {
		// given
		var jwt = userJwt("user", List.of("USER"), "openid");

		// when
		var result = mockMvc.perform(get("/api/members/me").with(jwt));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	void userCannotReadAnotherMemberProfile() throws Exception {
		// given
		Long adminId = memberProfileRepository.findOneByUserSubject("admin").orElseThrow().getId();

		// when
		var result = mockMvc.perform(get("/api/members/{id}", adminId)
				.with(userJwt("user", List.of("USER"), "member.read")));

		// then
		result.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("접근 거부"));
	}

	@Test
	void regularUserCannotAccessAdminApiEvenWithMemberScopes() throws Exception {
		// given
		var jwt = userJwt("user", List.of("USER"), "member.read member.write");

		// when
		var result = mockMvc.perform(get("/api/admin/members").with(jwt));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	void adminWithoutAdminScopeCannotAccessAdminApi() throws Exception {
		mockMvc.perform(get("/api/admin/members")
						.with(userJwt("admin", List.of("ADMIN"), "member.read")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminWithRoleAndScopeCanSearchMembers() throws Exception {
		mockMvc.perform(get("/api/admin/members")
						.param("status", "ACTIVE")
						.with(userJwt("admin", List.of("USER", "ADMIN"), "openid member.read admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.totalElements").isNumber());
	}

	@Test
	void optimisticLockConflictReturns409() throws Exception {
		var profile = memberProfileRepository.findOneByUserSubject("user").orElseThrow();
		Map<String, Object> body = Map.of(
				"version", profile.getVersion() + 99,
				"displayName", "Conflict Name",
				"address", Map.of(
						"countryCode", "KR",
						"city", "Seoul",
						"streetLine", "Teheran-ro 1",
						"postalCode", "06236"),
				"preferences", Map.of(
						"marketingOptIn", false,
						"locale", "ko-KR",
						"timezone", "Asia/Seoul"));

		mockMvc.perform(patch("/api/members/{id}", profile.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body))
						.with(userJwt("user", List.of("USER"), "member.write")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("낙관적 잠금 충돌"));
	}

	@Test
	void adminCanChangeMemberStatus() throws Exception {
		Long userId = memberProfileRepository.findOneByUserSubject("user").orElseThrow().getId();

		mockMvc.perform(post("/api/admin/members/{id}/status", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"SUSPENDED","reason":"policy check"}
								""")
						.with(userJwt("admin", List.of("ADMIN"), "admin member.write")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(MemberStatus.SUSPENDED.name()));

		assertThat(memberProfileRepository.findOneById(userId).orElseThrow().getStatus())
				.isEqualTo(MemberStatus.SUSPENDED);

		mockMvc.perform(post("/api/admin/members/{id}/status", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"status":"ACTIVE","reason":"restore after test"}
								""")
						.with(userJwt("admin", List.of("ADMIN"), "admin member.write")))
				.andExpect(status().isOk());
	}

	private static RequestPostProcessor userJwt(String subject, List<String> roles, String scope) {
		List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
		for (String token : scope.split(" ")) {
			if (!token.isBlank()) {
				authorities.add(new SimpleGrantedAuthority("SCOPE_" + token));
			}
		}
		for (String role : roles) {
			String value = role.startsWith("ROLE_") ? role : "ROLE_" + role;
			authorities.add(new SimpleGrantedAuthority(value));
		}
		return jwt()
				.jwt(builder -> builder
						.subject(subject)
						.claim("roles", roles)
						.claim("scope", scope)
						.claim("tenant_id", "tenant-demo")
						.audience(List.of("member-service")))
				.authorities(authorities.toArray(SimpleGrantedAuthority[]::new));
	}
}
