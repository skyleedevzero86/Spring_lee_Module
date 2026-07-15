package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import com.sleekydz86.loginstudy.auth.security.LoginProtectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityHardeningTest extends AuthServerIntegrationTestSupport {

	@DynamicPropertySource
	static void tightenLoginLock(DynamicPropertyRegistry registry) {
		registry.add("auth.login.max-failures", () -> "3");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegisteredClientRepository registeredClientRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private Environment environment;

	@BeforeEach
	void clearLoginLockKeys() {
		stringRedisTemplate.delete(LoginProtectionService.failureKey("user"));
		stringRedisTemplate.delete(LoginProtectionService.lockedKey("user"));
	}

	@Test
	void authorizeEndpointRejectsUnknownRedirectUri() throws Exception {
		// given
		String evilRedirect = "https://evil.example/callback";

		// when
		var result = mockMvc.perform(get("/oauth2/authorize")
				.with(user("user").roles("USER"))
				.param("response_type", "code")
				.param("client_id", AuthorizationServerConfig.CLIENT_USER_PORTAL)
				.param("redirect_uri", evilRedirect)
				.param("scope", "openid")
				.param("state", "abc123"));

		// then
		result.andExpect(status().isBadRequest());
	}

	@Test
	void registeredClientDoesNotUseWildcardRedirectUri() {
		// given
		RegisteredClient client = registeredClientRepository
				.findByClientId(AuthorizationServerConfig.CLIENT_USER_PORTAL);

		// when / then
		assertThat(client).isNotNull();
		assertThat(client.getRedirectUris())
				.containsExactly("http://localhost:8081/login/oauth2/code/user-portal");
		assertThat(client.getRedirectUris()).noneMatch(uri -> uri.contains("*"));
	}

	@Test
	void clientSecretIsStoredWithDelegatingPasswordEncoderPrefix() {
		// given
		String clientId = AuthorizationServerConfig.CLIENT_USER_PORTAL;

		// when
		String secret = jdbcTemplate.queryForObject(
				"SELECT client_secret FROM oauth2_registered_client WHERE client_id = ?",
				String.class,
				clientId);

		// then
		assertThat(secret).startsWith("{bcrypt}");
		assertThat(secret).doesNotContain(AuthDataInitializer.USER_PORTAL_SECRET);
	}

	@Test
	void refreshTokenReuseIsDisabledOnPortalClients() {
		// given
		RegisteredClient userPortal = registeredClientRepository
				.findByClientId(AuthorizationServerConfig.CLIENT_USER_PORTAL);
		RegisteredClient adminPortal = registeredClientRepository
				.findByClientId(AuthorizationServerConfig.CLIENT_ADMIN_PORTAL);

		// when / then
		assertThat(userPortal.getTokenSettings().isReuseRefreshTokens()).isFalse();
		assertThat(adminPortal.getTokenSettings().isReuseRefreshTokens()).isFalse();
	}

	@Test
	void bruteForceLoginLocksAccount() throws Exception {
		// given
		String username = "user";
		String wrongPassword = "wrong-password";

		// when
		for (int i = 0; i < 3; i++) {
			mockMvc.perform(formLogin("/login").user(username).password(wrongPassword))
					.andExpect(unauthenticated());
		}

		// then
		assertThat(stringRedisTemplate.hasKey(LoginProtectionService.lockedKey(username))).isTrue();
		mockMvc.perform(formLogin("/login").user(username).password(AuthDataInitializer.DEMO_USER_PASSWORD))
				.andExpect(unauthenticated());
	}

	@Test
	void sessionCookieSecurityFlagsAreConfigured() {
		// given / when
		String cookieName = environment.getProperty("server.servlet.session.cookie.name");
		String httpOnly = environment.getProperty("server.servlet.session.cookie.http-only");
		String sameSite = environment.getProperty("server.servlet.session.cookie.same-site");
		String exposure = environment.getProperty("management.endpoints.web.exposure.include");

		// then
		assertThat(cookieName).isEqualTo("AUTHSESSION");
		assertThat(httpOnly).isEqualTo("true");
		assertThat(sameSite).isEqualToIgnoringCase("lax");
		assertThat(exposure).isEqualTo("health,info");
	}

	@Test
	void actuatorEnvIsNotExposed() throws Exception {
		// given
		String envEndpoint = "/actuator/env";

		// when
		var result = mockMvc.perform(get(envEndpoint));

		// then
		result.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void clientCredentialsWithWrongSecretIsRejected() throws Exception {
		// given
		String wrongSecret = "not-the-secret";

		// when
		var result = mockMvc.perform(post("/oauth2/token")
				.with(httpBasic(AuthorizationServerConfig.CLIENT_MEMBER_SERVICE, wrongSecret))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("grant_type", "client_credentials")
				.param("scope", "member.read"));

		// then
		result.andExpect(status().isUnauthorized());
	}

	@Test
	void actuatorHealthIsPublicWithoutDetails() throws Exception {
		// given
		String healthEndpoint = "/actuator/health";

		// when
		var body = mockMvc.perform(get(healthEndpoint))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		// then
		assertThat(body).doesNotContain("\"components\"");
	}
}
