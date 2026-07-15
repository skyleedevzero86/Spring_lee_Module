package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sleekydz86.loginstudy.auth.config.AuthDataInitializer;
import com.sleekydz86.loginstudy.auth.config.AuthorizationServerConfig;
import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2AuthorizationPersistenceTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RegisteredClientRepository registeredClientRepository;

	@Autowired
	private OAuth2AuthorizationConsentService authorizationConsentService;

	@Autowired
	private AuthPersistenceQueryService authPersistenceQueryService;

	@Test
	void clientCredentialsAuthorizationIsPersisted() throws Exception {
		authPersistenceQueryService.assertJdbcBackedServices();

		Long before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM oauth2_authorization", Long.class);

		mockMvc.perform(post("/oauth2/token")
						.with(httpBasic(
								AuthorizationServerConfig.CLIENT_MEMBER_SERVICE,
								AuthDataInitializer.MEMBER_SERVICE_SECRET))
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("grant_type", "client_credentials")
						.param("scope", "member.read"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isNotEmpty());

		Long after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM oauth2_authorization", Long.class);
		assertThat(after).isGreaterThan(before);

		Integer grantRows = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM oauth2_authorization
				WHERE authorization_grant_type = ?
				  AND principal_name = ?
				""",
				Integer.class,
				AuthorizationGrantType.CLIENT_CREDENTIALS.getValue(),
				AuthorizationServerConfig.CLIENT_MEMBER_SERVICE);
		assertThat(grantRows).isGreaterThan(0);
	}

	@Test
	void authorizationConsentIsPersisted() {
		RegisteredClient client = registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_USER_PORTAL);
		assertThat(client).isNotNull();

		OAuth2AuthorizationConsent consent = OAuth2AuthorizationConsent
				.withId(client.getId(), "user")
				.scope("openid")
				.scope("profile")
				.scope("member.read")
				.build();
		authorizationConsentService.save(consent);

		OAuth2AuthorizationConsent loaded = authorizationConsentService.findById(client.getId(), "user");
		assertThat(loaded).isNotNull();
		assertThat(loaded.getScopes()).contains("member.read", "profile");

		Integer dbCount = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM oauth2_authorization_consent
				WHERE registered_client_id = ?
				  AND principal_name = ?
				""",
				Integer.class,
				client.getId(),
				"user");
		assertThat(dbCount).isEqualTo(1);
	}
}
