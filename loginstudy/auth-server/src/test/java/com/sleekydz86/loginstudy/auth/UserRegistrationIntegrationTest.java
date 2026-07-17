package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationIntegrationTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원가입은 약관 동의를 요구하고 암호화된 사용자 계정을 생성한다")
	void registrationRequiresAgreementsAndCreatesEncryptedUserAccount() throws Exception {
		// given / when
		mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("register-terms"));

		mockMvc.perform(post("/register/terms").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("register-terms"))
				.andExpect(model().attributeExists("agreementError"));

		mockMvc.perform(post("/register/terms")
						.with(csrf())
						.param("termsAccepted", "true")
						.param("privacyAccepted", "true"))
				.andExpect(status().isOk())
				.andExpect(view().name("register-details"))
				.andExpect(model().attributeExists("registrationForm"));

		String username = "signup_user";
		String password = "SecurePass123!";

		mockMvc.perform(registrationRequest(username, password))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/register/complete"));

		// then
		UserAccount account = userAccountRepository.findByUsername(username).orElseThrow();
		assertThat(passwordEncoder.matches(password, account.getPassword())).isTrue();
		assertThat(account.getEmail()).isEqualTo("signup_user@example.com");
		assertThat(account.getDisplayName()).isEqualTo("신규 회원");
		assertThat(account.getPhone()).isEqualTo("010-1234-5678");
		assertThat(account.getMemberType()).isEqualTo("PERSONAL");
		assertThat(account.getTermsAcceptedAt()).isNotNull();
		assertThat(account.getPrivacyAcceptedAt()).isNotNull();
		assertThat(account.getRoles())
				.extracting(role -> role.getRole())
				.containsExactly("USER");
	}

	private MockHttpServletRequestBuilder registrationRequest(
			String username,
			String password) {
		return post("/register")
				.with(csrf())
				.param("termsAccepted", "true")
				.param("privacyAccepted", "true")
				.param("username", username)
				.param("password", password)
				.param("passwordConfirm", password)
				.param("email", username + "@example.com")
				.param("displayName", "신규 회원")
				.param("phone", "010-1234-5678")
				.param("memberType", "PERSONAL");
	}
}
