package com.sleekydz86.loginstudy.adminportal.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

class OidcRolesAuthoritiesMapperTest {

	private final OidcRolesAuthoritiesMapper mapper = new OidcRolesAuthoritiesMapper();

	@Test
	void mapsRolesClaimCollectionToRoleAuthorities() {
		// given
		OidcIdToken idToken = new OidcIdToken(
				"token-value",
				java.time.Instant.now(),
				java.time.Instant.now().plusSeconds(3600),
				Map.of(
						"sub", "admin",
						"roles", List.of("ADMIN", "USER")));
		OidcUserAuthority oidcAuthority = new OidcUserAuthority(idToken);

		// when
		var mapped = mapper.mapAuthorities(Set.of(oidcAuthority));

		// then
		assertThat(mapped.stream().map(GrantedAuthority::getAuthority))
				.contains("ROLE_ADMIN", "ROLE_USER", "OIDC_USER");
	}

	@Test
	void mapsCommaSeparatedRolesString() {
		// given
		OidcIdToken idToken = new OidcIdToken(
				"token-value",
				java.time.Instant.now(),
				java.time.Instant.now().plusSeconds(3600),
				Map.of(
						"sub", "admin",
						"roles", "ADMIN, USER"));
		OidcUserAuthority oidcAuthority = new OidcUserAuthority(idToken);

		// when
		var mapped = mapper.mapAuthorities(List.of(oidcAuthority, new SimpleGrantedAuthority("SCOPE_openid")));

		// then
		assertThat(mapped.stream().map(GrantedAuthority::getAuthority))
				.contains("ROLE_ADMIN", "ROLE_USER", "SCOPE_openid");
	}
}
