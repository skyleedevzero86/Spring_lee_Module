package com.sleekydz86.loginstudy.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class MemberSecurityHardeningTest extends MemberRedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberProfileRepository memberProfileRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void sqlInjectionPayloadInNameFilterDoesNotBypassSearch() throws Exception {
		// given
		String sqlInjectionPayload = "x' OR '1'='1";

		// when
		var result = mockMvc.perform(get("/api/admin/members")
				.param("name", sqlInjectionPayload)
				.with(adminJwt()));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void corsAllowsTrustedPortalOrigin() throws Exception {
		// given
		String trustedOrigin = "http://localhost:8081";

		// when
		var result = mockMvc.perform(options("/api/members/me")
				.header(HttpHeaders.ORIGIN, trustedOrigin)
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"));

		// then
		result.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, trustedOrigin))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
	}

	@Test
	void corsRejectsUntrustedOrigin() throws Exception {
		// given
		String evilOrigin = "https://evil.example";

		// when
		var result = mockMvc.perform(get("/api/members/me")
				.header(HttpHeaders.ORIGIN, evilOrigin)
				.with(userJwt("user", List.of("USER"), "member.read")));

		// then
		result.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}

	@Test
	void actuatorEnvIsNotExposed() throws Exception {
		// given
		String envEndpoint = "/actuator/env";

		// when
		var result = mockMvc.perform(get(envEndpoint));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@Test
	void actuatorHealthDoesNotRevealComponentDetails() throws Exception {
		// given
		String healthEndpoint = "/actuator/health";

		// when
		var response = mockMvc.perform(get(healthEndpoint))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		// then
		assertThat(response).contains("\"status\"");
		assertThat(response).doesNotContain("\"components\"");
		assertThat(response).doesNotContain("loginstudy");
	}

	@Test
	void userCannotPatchAnotherMemberProfile() throws Exception {
		// given
		Long adminId = memberProfileRepository.findOneByUserSubject("admin").orElseThrow().getId();
		var body = Map.of(
				"version", 0L,
				"displayName", "Hacked",
				"address", Map.of(
						"countryCode", "KR",
						"city", "Seoul",
						"streetLine", "Attack",
						"postalCode", "00000"),
				"preferences", Map.of(
						"marketingOptIn", false,
						"locale", "ko-KR",
						"timezone", "Asia/Seoul"));

		// when
		var result = mockMvc.perform(patch("/api/members/{id}", adminId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(userJwt("user", List.of("USER"), "member.write")));

		// then
		result.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.title").value("접근 거부"));
	}

	@Test
	void roleClaimWithoutAdminScopeCannotEscalateToAdminApi() throws Exception {
		// given
		RequestPostProcessor spoofedAdminRole = userJwt("user", List.of("ADMIN"), "member.read");

		// when
		var result = mockMvc.perform(get("/api/admin/members").with(spoofedAdminRole));

		// then
		result.andExpect(status().isForbidden());
	}

	private static RequestPostProcessor adminJwt() {
		return userJwt("admin", List.of("USER", "ADMIN"), "openid member.read admin");
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
