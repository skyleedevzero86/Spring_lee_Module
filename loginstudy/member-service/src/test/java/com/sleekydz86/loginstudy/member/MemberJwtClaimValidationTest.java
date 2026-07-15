package com.sleekydz86.loginstudy.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

class MemberJwtClaimValidationTest {

	@Test
	void issuerValidatorRejectsWrongIssuer() {
		// given
		JwtIssuerValidator validator = new JwtIssuerValidator("http://localhost:9000");
		Jwt jwt = sampleJwt(
				"http://evil.example",
				List.of("member-service"),
				Instant.now().minusSeconds(30),
				Instant.now().plusSeconds(60));

		// when
		OAuth2TokenValidatorResult result = validator.validate(jwt);

		// then
		assertThat(result.hasErrors()).isTrue();
		assertThat(result.getErrors()).extracting(OAuth2Error::getErrorCode).contains("invalid_token");
	}

	@Test
	void audienceValidatorRejectsWrongAudience() {
		// given
		JwtClaimValidator<List<String>> audienceValidator = new JwtClaimValidator<>(
				JwtClaimNames.AUD,
				aud -> aud != null && aud.contains("member-service"));
		Jwt jwt = sampleJwt(
				"http://localhost:9000",
				List.of("other-service"),
				Instant.now().minusSeconds(30),
				Instant.now().plusSeconds(60));

		// when
		OAuth2TokenValidatorResult result = audienceValidator.validate(jwt);

		// then
		assertThat(result.hasErrors()).isTrue();
	}

	@Test
	void timestampValidatorRejectsExpiredToken() {
		// given
		JwtTimestampValidator validator = new JwtTimestampValidator();
		Instant expiredAt = Instant.now().minusSeconds(120);
		Jwt jwt = sampleJwt(
				"http://localhost:9000",
				List.of("member-service"),
				expiredAt.minusSeconds(60),
				expiredAt);

		// when
		OAuth2TokenValidatorResult result = validator.validate(jwt);

		// then
		assertThat(result.hasErrors()).isTrue();
	}

	@Test
	void validTokenPassesIssuerAudienceAndExpiryChecks() {
		// given
		Instant issuedAt = Instant.now().minusSeconds(30);
		Instant expiresAt = Instant.now().plusSeconds(300);
		Jwt jwt = sampleJwt("http://localhost:9000", List.of("member-service"), issuedAt, expiresAt);

		// when / then
		assertThat(new JwtIssuerValidator("http://localhost:9000").validate(jwt).hasErrors()).isFalse();
		assertThat(new JwtClaimValidator<List<String>>(
						JwtClaimNames.AUD,
						aud -> aud != null && aud.contains("member-service"))
				.validate(jwt)
				.hasErrors()).isFalse();
		assertThat(new JwtTimestampValidator().validate(jwt).hasErrors()).isFalse();
	}

	private static Jwt sampleJwt(String issuer, List<String> audience, Instant issuedAt, Instant expiresAt) {
		return Jwt.withTokenValue("test-token")
				.header("alg", "none")
				.issuer(issuer)
				.audience(audience)
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.notBefore(issuedAt)
				.subject("user")
				.claim("scope", "member.read")
				.build();
	}
}
