package com.sleekydz86.loginstudy.member;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class MemberSearchTuningTest extends MemberRedisTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberProfileRepository memberProfileRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void offsetSearchSupportsStatusAndNameFilter() throws Exception {
		seed("tune-a", "tune-a@loginstudy.local", "Tune Alpha", MemberStatus.ACTIVE);
		seed("tune-b", "tune-b@loginstudy.local", "Tune Beta", MemberStatus.SUSPENDED);

		mockMvc.perform(get("/api/admin/members")
						.param("status", "ACTIVE")
						.param("name", "Alpha")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].displayName").value("Tune Alpha"));
	}

	@Test
	void keysetSearchReturnsNextCursor() throws Exception {
		for (int i = 0; i < 5; i++) {
			seed("keyset-" + i, "keyset-" + i + "@loginstudy.local", "Keyset " + i, MemberStatus.ACTIVE);
		}

		MvcResult first = mockMvc.perform(get("/api/admin/members/keyset")
						.param("status", "ACTIVE")
						.param("size", "2")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.nextCursorId").isNumber())
				.andReturn();

		JsonNode body = objectMapper.readTree(first.getResponse().getContentAsString());
		String cursorId = body.get("nextCursorId").asText();
		String cursorJoinedAt = body.get("nextCursorJoinedAt").asText();

		mockMvc.perform(get("/api/admin/members/keyset")
						.param("status", "ACTIVE")
						.param("size", "2")
						.param("cursorId", cursorId)
						.param("cursorJoinedAt", cursorJoinedAt)
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2));
	}

	private void seed(String subject, String email, String name, MemberStatus status) {
		if (memberProfileRepository.existsByUserSubject(subject)) {
			return;
		}
		memberProfileRepository.save(new MemberProfile(subject, email, name, status, "tenant-demo"));
	}

	private static org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
		return jwt().jwt(token -> token
						.subject("admin")
						.claim("roles", List.of("ADMIN"))
						.claim("scope", "admin member.read"))
				.authorities(
						new SimpleGrantedAuthority("SCOPE_admin"),
						new SimpleGrantedAuthority("ROLE_ADMIN"),
						new SimpleGrantedAuthority("SCOPE_member.read"));
	}
}
