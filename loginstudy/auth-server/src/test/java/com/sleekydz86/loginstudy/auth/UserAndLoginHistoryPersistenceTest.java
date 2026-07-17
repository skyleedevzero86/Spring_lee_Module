package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.repository.LoginHistoryRepository;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserAndLoginHistoryPersistenceTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private LoginHistoryRepository loginHistoryRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthPersistenceQueryService authPersistenceQueryService;

	@Test
	@DisplayName("초기 사용자를 BCrypt 비밀번호와 함께 데이터베이스에서 조회한다")
	void seededUserIsLoadedFromDatabaseWithBcryptPassword() {
		// when
		var user = userAccountRepository.findByUsername("user").orElseThrow();
		// then
		assertThat(user.getPassword()).startsWith("{bcrypt}");
		assertThat(passwordEncoder.matches(AuthDataInitializer.DEMO_USER_PASSWORD, user.getPassword())).isTrue();
		assertThat(user.getEmail()).isEqualTo("user@loginstudy.local");
		assertThat(user.getTenantId()).isEqualTo("tenant-demo");
		assertThat(user.getRoles()).extracting("role").contains("USER");
	}

	@Test
	@DisplayName("로그인 성공과 실패가 로그인 이력에 기록된다")
	void successfulAndFailedLoginsAreWrittenToLoginHistory() throws Exception {
		// given
		long successBefore = loginHistoryRepository.countByUsernameAndSuccess("user", true);
		long failureBefore = loginHistoryRepository.countByUsernameAndSuccess("user", false);

		// when
		mockMvc.perform(formLogin("/login").user("user").password(AuthDataInitializer.DEMO_USER_PASSWORD))
				.andExpect(authenticated().withUsername("user"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));

		mockMvc.perform(formLogin("/login").user("user").password("wrong-password"))
				.andExpect(unauthenticated())
				.andExpect(status().is3xxRedirection());

		// then
		assertThat(loginHistoryRepository.countByUsernameAndSuccess("user", true)).isGreaterThan(successBefore);
		assertThat(loginHistoryRepository.countByUsernameAndSuccess("user", false)).isGreaterThan(failureBefore);

		var history = authPersistenceQueryService.findLoginHistory("user", 10);
		assertThat(history).isNotEmpty();
		assertThat(history.getFirst().username()).isEqualTo("user");
	}
}
