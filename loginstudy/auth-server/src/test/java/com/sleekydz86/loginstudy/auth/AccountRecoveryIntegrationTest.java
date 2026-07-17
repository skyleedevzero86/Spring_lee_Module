package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountRecoveryIntegrationTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원정보로 아이디를 찾고 검증 세션으로 비밀번호를 재설정한다")
	void findsIdAndResetsPasswordAfterIdentityVerification() throws Exception {
		// given
		String username = "recovery_user";
		String oldPassword = "OldPassword123!";
		String newPassword = "NewPassword456!";
		UserAccount account = new UserAccount(
				username,
				passwordEncoder.encode(oldPassword),
				"recovery@example.com",
				"tenant-demo",
				"복구 회원",
				"010-2222-3333",
				"PERSONAL",
				Instant.now());
		account.addRole("USER");
		userAccountRepository.saveAndFlush(account);

		// when
		MvcResult idLookupResult = mockMvc.perform(post("/account-recovery/id")
						.with(csrf())
						.param("displayName", "복구 회원")
						.param("phone", "010-2222-3333"))
				.andExpect(status().isOk())
				.andExpect(view().name("find-id"))
				.andReturn();

		// then
		Object usernames = idLookupResult.getModelAndView().getModel().get("usernames");
		assertThat(usernames).isEqualTo(List.of(username));

		// when
		MvcResult verificationResult = mockMvc.perform(post("/account-recovery/password/verify")
						.with(csrf())
						.param("username", username)
						.param("email", "recovery@example.com")
						.param("phone", "010-2222-3333"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/account-recovery/password/reset"))
				.andReturn();
		Cookie[] cookies = verificationResult.getResponse().getCookies();
		assertThat(cookies).isNotEmpty();
		Cookie sessionCookie = cookies[0];

		mockMvc.perform(post("/account-recovery/password/reset")
						.cookie(sessionCookie)
						.with(csrf())
						.param("password", newPassword)
						.param("passwordConfirm", newPassword))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login?passwordReset"));

		// then
		UserAccount updatedAccount = userAccountRepository.findByUsername(username).orElseThrow();
		assertThat(passwordEncoder.matches(newPassword, updatedAccount.getPassword())).isTrue();
		assertThat(passwordEncoder.matches(oldPassword, updatedAccount.getPassword())).isFalse();
	}

	@Test
	@DisplayName("본인정보 확인 없이 비밀번호 재설정 화면에 접근하면 확인 화면으로 이동한다")
	void rejectsPasswordResetWithoutVerification() throws Exception {
		// when
		MvcResult result = mockMvc.perform(get("/account-recovery/password/reset"))
				.andReturn();

		// then
		assertThat(result.getResponse().getStatus()).isBetween(300, 399);
		assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/account-recovery/password");
	}

	@Test
	@DisplayName("CSRF 토큰 없는 계정 찾기 요청을 거부한다")
	void rejectsRecoveryRequestWithoutCsrf() throws Exception {
		// when
		MvcResult result = mockMvc.perform(post("/account-recovery/id")
						.param("displayName", "복구 회원")
						.param("phone", "010-2222-3333"))
				.andReturn();

		// then
		assertThat(result.getResponse().getStatus()).isEqualTo(403);
	}
}
