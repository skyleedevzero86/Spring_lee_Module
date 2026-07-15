package com.sleekydz86.loginstudy.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Import(SignedJwtMemberResourceIntegrationTest.SignedJwtITConfig.class)
@SpringBootTest(properties = {
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=",
		"spring.main.allow-bean-definition-overriding=true"
})
@AutoConfigureMockMvc
class SignedJwtMemberResourceIntegrationTest extends MemberRedisTestSupport {

	private static final String ISSUER = "http://localhost:9000";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder;

	@DynamicPropertySource
	static void clearIssuerUri(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "");
		registry.add("spring.security.oauth2.resourceserver.jwt.audiences", () -> "member-service");
	}

	@Test
	void signedAccessTokenAllowsReadingOwnProfile() throws Exception {
		// given
		String token = mintAccessToken("user", List.of("USER"), "openid member.read");

		// when
		var result = mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token));

		// then
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.userSubject").value("user"))
				.andExpect(jsonPath("$.email").value("user@loginstudy.local"));
	}

	@Test
	void signedAccessTokenWithoutMemberReadScopeIsForbidden() throws Exception {
		// given
		String token = mintAccessToken("user", List.of("USER"), "openid");

		// when
		var result = mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token));

		// then
		result.andExpect(status().isForbidden());
	}

	@Test
	void expiredSignedAccessTokenIsUnauthorized() throws Exception {
		// given
		Instant expired = Instant.now().minusSeconds(120);
		String token = mintAccessToken("user", List.of("USER"), "member.read", expired.minusSeconds(60), expired);

		// when
		var result = mockMvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token));

		// then
		result.andExpect(status().isUnauthorized());
	}

	private String mintAccessToken(String subject, List<String> roles, String scope) {
		Instant issuedAt = Instant.now().minusSeconds(5);
		Instant expiresAt = Instant.now().plusSeconds(300);
		return mintAccessToken(subject, roles, scope, issuedAt, expiresAt);
	}

	private String mintAccessToken(
			String subject,
			List<String> roles,
			String scope,
			Instant issuedAt,
			Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(ISSUER)
				.subject(subject)
				.audience(List.of("member-service"))
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.notBefore(issuedAt)
				.claim("scope", scope)
				.claim("roles", roles)
				.claim("tenant_id", "tenant-demo")
				.build();
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class SignedJwtITConfig {

		private static final KeyPair KEY_PAIR = generateRsaKey();

		@Bean
		@ServiceConnection
		PostgreSQLContainer postgresContainer() {
			return new PostgreSQLContainer(DockerImageName.parse("postgres:16.9-alpine"));
		}

		@Bean
		@Primary
		JwtDecoder jwtDecoder() {
			RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
			return NimbusJwtDecoder.withPublicKey(publicKey).build();
		}

		@Bean
		JwtEncoder jwtEncoder() {
			RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) KEY_PAIR.getPrivate();
			RSAKey rsaKey = new RSAKey.Builder(publicKey)
					.privateKey(privateKey)
					.keyID(UUID.randomUUID().toString())
					.build();
			return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
		}

		private static KeyPair generateRsaKey() {
			try {
				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
				generator.initialize(2048);
				return generator.generateKeyPair();
			}
			catch (Exception ex) {
				throw new IllegalStateException("RSA 키 생성에 실패했습니다", ex);
			}
		}
	}
}
