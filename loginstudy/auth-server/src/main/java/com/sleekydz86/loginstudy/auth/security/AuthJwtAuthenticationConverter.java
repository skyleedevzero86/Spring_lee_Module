package com.sleekydz86.loginstudy.auth.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class AuthJwtAuthenticationConverter {

	private AuthJwtAuthenticationConverter() {
	}

	public static Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
		JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
		delegate.setJwtGrantedAuthoritiesConverter(AuthJwtAuthenticationConverter::extractAuthorities);
		return jwt -> {
			AbstractAuthenticationToken token = delegate.convert(jwt);
			Collection<GrantedAuthority> authorities = token == null ? List.of() : token.getAuthorities();
			return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
		};
	}

	private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Set<GrantedAuthority> authorities = new HashSet<>();
		Object scopeClaim = jwt.getClaims().containsKey("scope") ? jwt.getClaim("scope") : jwt.getClaim("scp");
		if (scopeClaim instanceof String scopeString) {
			for (String scope : scopeString.split(" ")) {
				if (!scope.isBlank()) {
					authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
				}
			}
		}
		else if (scopeClaim instanceof Collection<?> scopes) {
			for (Object scope : scopes) {
				authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
			}
		}
		Object rolesClaim = jwt.getClaim("roles");
		if (rolesClaim instanceof Collection<?> roles) {
			for (Object role : roles) {
				String value = String.valueOf(role).toUpperCase(Locale.ROOT);
				authorities.add(new SimpleGrantedAuthority(
						value.startsWith("ROLE_") ? value : "ROLE_" + value));
			}
		}
		return new ArrayList<>(authorities);
	}
}
