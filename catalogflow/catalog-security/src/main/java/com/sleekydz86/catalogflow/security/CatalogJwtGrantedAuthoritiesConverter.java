package com.sleekydz86.catalogflow.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class CatalogJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.addAll(extractScopes(jwt));
		authorities.addAll(extractRoles(jwt));
		return List.copyOf(authorities);
	}

	private Collection<GrantedAuthority> extractScopes(Jwt jwt) {
		List<GrantedAuthority> scopes = new ArrayList<>();
		Object scopeClaim = jwt.getClaim("scope");
		if (scopeClaim instanceof String scopeString) {
			for (String scope : scopeString.split(" ")) {
				addScope(scopes, scope);
			}
		}
		Object scpClaim = jwt.getClaim("scp");
		if (scpClaim instanceof Collection<?> scpValues) {
			for (Object value : scpValues) {
				addScope(scopes, String.valueOf(value));
			}
		}
		return scopes;
	}

	private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
		List<GrantedAuthority> roles = new ArrayList<>();
		Object rolesClaim = jwt.getClaim("roles");
		if (rolesClaim instanceof Collection<?> roleValues) {
			for (Object value : roleValues) {
				addRole(roles, String.valueOf(value));
			}
		}
		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof Map<?, ?> realmAccessMap) {
			Object realmRoles = realmAccessMap.get("roles");
			if (realmRoles instanceof Collection<?> realmRoleValues) {
				for (Object value : realmRoleValues) {
					addRole(roles, String.valueOf(value));
				}
			}
		}
		return roles;
	}

	private void addScope(List<GrantedAuthority> authorities, String scope) {
		if (scope == null || scope.isBlank()) {
			return;
		}
		String normalized = scope.startsWith("SCOPE_") ? scope.substring(6) : scope;
		authorities.add(new SimpleGrantedAuthority("SCOPE_" + normalized));
	}

	private void addRole(List<GrantedAuthority> authorities, String role) {
		if (role == null || role.isBlank()) {
			return;
		}
		String normalized = role.startsWith("ROLE_") ? role.substring(5) : role;
		authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
	}
}
