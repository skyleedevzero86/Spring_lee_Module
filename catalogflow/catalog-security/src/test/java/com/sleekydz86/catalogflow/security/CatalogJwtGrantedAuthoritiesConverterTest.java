package com.sleekydz86.catalogflow.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class CatalogJwtGrantedAuthoritiesConverterTest {

	private final CatalogJwtGrantedAuthoritiesConverter converter = new CatalogJwtGrantedAuthoritiesConverter();

	@Test
	@DisplayName("JWT scope 클레임을 SCOPE 권한으로 변환한다")
	void shouldConvertScopeClaim() {
		// given
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("scope", "catalog.read catalog.write")
				.issuedAt(Instant.parse("2026-07-16T00:00:00Z"))
				.expiresAt(Instant.parse("2026-07-16T01:00:00Z"))
				.build();

		// when
		Collection<GrantedAuthority> authorities = converter.convert(jwt);

		// then
		assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals(CatalogScopes.SCOPE_READ)));
		assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals(CatalogScopes.SCOPE_WRITE)));
	}

	@Test
	@DisplayName("Keycloak realm_access 역할을 ROLE 권한으로 변환한다")
	void shouldConvertRealmRoles() {
		// given
		Jwt jwt = Jwt.withTokenValue("token")
				.header("alg", "none")
				.claim("realm_access", Map.of("roles", List.of("CATALOG_EDITOR", "SYSTEM_ADMIN")))
				.issuedAt(Instant.parse("2026-07-16T00:00:00Z"))
				.expiresAt(Instant.parse("2026-07-16T01:00:00Z"))
				.build();

		// when
		Collection<GrantedAuthority> authorities = converter.convert(jwt);

		// then
		assertEquals(2, authorities.size());
		assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals(CatalogRoles.ROLE_EDITOR)));
		assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals(CatalogRoles.ROLE_SYSTEM_ADMIN)));
	}
}
