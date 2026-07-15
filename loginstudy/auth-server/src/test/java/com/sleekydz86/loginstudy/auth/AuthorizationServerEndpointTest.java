package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationServerEndpointTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegisteredClientRepository registeredClientRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void openIdConfigurationAndJwksArePublished() throws Exception {
		mockMvc.perform(get("/.well-known/openid-configuration"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.issuer").value("http://localhost:9000"))
				.andExpect(jsonPath("$.authorization_endpoint").exists())
				.andExpect(jsonPath("$.token_endpoint").exists())
				.andExpect(jsonPath("$.jwks_uri").exists())
				.andExpect(jsonPath("$.userinfo_endpoint").exists())
				.andExpect(jsonPath("$.revocation_endpoint").exists());

		mockMvc.perform(get("/oauth2/jwks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.keys").isArray())
				.andExpect(jsonPath("$.keys[0].kty").value("RSA"));
	}

	@Test
	void seededClientsAndUsersUseDelegatingPasswordEncoder() {
		assertThat(registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_USER_PORTAL))
				.isNotNull();
		assertThat(registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_ADMIN_PORTAL))
				.isNotNull();
		assertThat(registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_MEMBER_SERVICE))
				.isNotNull();

		assertThat(userAccountRepository.findByUsername("user")).isPresent();
		assertThat(userAccountRepository.findByUsername("admin")).isPresent();

		String userPassword = userAccountRepository.findByUsername("user").orElseThrow().getPassword();
		assertThat(userPassword).startsWith("{bcrypt}");
		assertThat(passwordEncoder.matches(AuthDataInitializer.DEMO_USER_PASSWORD, userPassword)).isTrue();

		Integer clientCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM oauth2_registered_client",
				Integer.class);
		assertThat(clientCount).isGreaterThanOrEqualTo(3);
	}

	@Test
	void clientCredentialsGrantIssuesAccessToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/oauth2/token")
						.with(httpBasic(
								AuthorizationServerConfig.CLIENT_MEMBER_SERVICE,
								AuthDataInitializer.MEMBER_SERVICE_SECRET))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("grant_type", "client_credentials")
						.param("scope", "member.read"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isNotEmpty())
				.andExpect(jsonPath("$.token_type").value("Bearer"))
				.andReturn();

		assertThat(result.getResponse().getContentAsString()).contains("access_token");
	}

	@Test
	void loginPageIsAccessible() throws Exception {
		mockMvc.perform(get("/login"))
				.andExpect(status().isOk());
	}
}
