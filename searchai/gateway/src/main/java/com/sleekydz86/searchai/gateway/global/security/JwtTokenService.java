package com.sleekydz86.searchai.gateway.global.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public final class JwtTokenService {

	private final SecurityProperties properties;

	public JwtTokenService(SecurityProperties properties) {
		this.properties = properties;
	}

	public String issue(AuthUser user) {
		long expiresAt = Instant.now().getEpochSecond() + properties.tokenTtlSeconds();
		String payload = user.username() + "|" + user.role().name() + "|" + expiresAt;
		String encodedPayload = Base64.getUrlEncoder().withoutPadding()
			.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
		return encodedPayload + "." + sign(encodedPayload);
	}

	public Optional<AuthUser> parse(String token) {
		if (token == null || token.isBlank() || !token.contains(".")) {
			return Optional.empty();
		}
		String[] parts = token.split("\\.", 2);
		if (parts.length != 2) {
			return Optional.empty();
		}
		String encodedPayload = parts[0];
		String signature = parts[1];
		if (!sign(encodedPayload).equals(signature)) {
			return Optional.empty();
		}
		String payload = new String(Base64.getUrlDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
		String[] fields = payload.split("\\|", 3);
		if (fields.length != 3) {
			return Optional.empty();
		}
		long expiresAt = Long.parseLong(fields[2]);
		if (Instant.now().getEpochSecond() > expiresAt) {
			return Optional.empty();
		}
		return Optional.of(new AuthUser(fields[0], UserRole.valueOf(fields[1])));
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(properties.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			throw new IllegalStateException("토큰 서명에 실패했습니다", ex);
		}
	}
}
