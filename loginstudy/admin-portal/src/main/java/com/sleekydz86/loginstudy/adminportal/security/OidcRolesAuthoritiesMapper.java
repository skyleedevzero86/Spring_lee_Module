package com.sleekydz86.loginstudy.adminportal.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

@Component
public class OidcRolesAuthoritiesMapper implements GrantedAuthoritiesMapper {

	@Override
	public Collection<? extends GrantedAuthority> mapAuthorities(
			Collection<? extends GrantedAuthority> authorities) {
		Set<GrantedAuthority> mapped = new HashSet<>(authorities);

		for (GrantedAuthority authority : authorities) {
			Map<String, Object> claims = Map.of();
			if (authority instanceof OidcUserAuthority oidcUserAuthority) {
				claims = oidcUserAuthority.getIdToken().getClaims();
			}
			else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
				claims = oauth2UserAuthority.getAttributes();
			}
			addRoles(mapped, claims.get("roles"));
		}
		return mapped;
	}

	private static void addRoles(Set<GrantedAuthority> mapped, Object rolesClaim) {
		if (rolesClaim instanceof Collection<?> roles) {
			for (Object role : roles) {
				mapped.add(toRoleAuthority(String.valueOf(role)));
			}
		}
		else if (rolesClaim instanceof String rolesString) {
			for (String role : rolesString.split("[,\\s]+")) {
				if (!role.isBlank()) {
					mapped.add(toRoleAuthority(role));
				}
			}
		}
	}

	private static SimpleGrantedAuthority toRoleAuthority(String role) {
		String value = role.toUpperCase(Locale.ROOT);
		if (!value.startsWith("ROLE_")) {
			value = "ROLE_" + value;
		}
		return new SimpleGrantedAuthority(value);
	}
}
