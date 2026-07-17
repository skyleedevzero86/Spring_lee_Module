package com.sleekydz86.loginstudy.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
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

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("오프셋 검색은 상태와 이름 필터를 지원한다")
	void offsetSearchSupportsStatusAndNameFilter() throws Exception {
		// given
		seed("tune-a", "tune-a@loginstudy.local", "Tune Alpha", MemberStatus.ACTIVE);
		seed("tune-b", "tune-b@loginstudy.local", "Tune Beta", MemberStatus.SUSPENDED);

		// when / then
		mockMvc.perform(get("/api/admin/members")
						.param("status", "ACTIVE")
						.param("name", "Alpha")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].displayName").value("T*a"));
	}

	@Test
	@DisplayName("관리자 목록은 민감정보를 마스킹하고 선택한 필드만 복원한다")
	void memberListMasksSensitiveDataAndRevealReturnsOnlySelectedField() throws Exception {
		// given
		seed("privacy-user", "privacy@loginstudy.local", "홍길동", MemberStatus.ACTIVE);

		// when / then
		mockMvc.perform(get("/api/admin/members")
						.param("email", "privacy@loginstudy.local")
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].email").value("p***@loginstudy.local"))
				.andExpect(jsonPath("$.content[0].displayName").value("홍*동"));

		Long memberId = jdbcTemplate.queryForObject(
				"SELECT id FROM member_profile WHERE user_subject = ?",
				Long.class,
				"privacy-user");

		mockMvc.perform(post("/api/admin/members/{id}/sensitive/DISPLAY_NAME/reveal", memberId)
						.with(adminJwt()))
				.andExpect(status().isOk())
				.andExpect(header().string("Cache-Control", "no-store"))
				.andExpect(jsonPath("$.value").value("홍길동"))
				.andExpect(jsonPath("$.email").doesNotExist());

		String storedName = jdbcTemplate.queryForObject(
				"SELECT display_name FROM member_profile WHERE id = ?",
				String.class,
				memberId);
		assertThat(storedName)
				.startsWith("enc:v1:")
				.doesNotContain("홍길동");
	}

	@Test
	@DisplayName("키셋 검색은 다음 커서를 반환한다")
	void keysetSearchReturnsNextCursor() throws Exception {
		// given
		for (int i = 0; i < 5; i++) {
			seed("keyset-" + i, "keyset-" + i + "@loginstudy.local", "Keyset " + i, MemberStatus.ACTIVE);
		}

		// when / then
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
