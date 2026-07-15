package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.security.AuthUser;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class JwtTokenCustomizerConfig {

	public static final String AUDIENCE_MEMBER_SERVICE = "member-service";

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
		return context -> {
			Authentication principal = context.getPrincipal();
			Object userPrincipal = principal.getPrincipal();

			List<String> audience = List.of(
					AUDIENCE_MEMBER_SERVICE,
					context.getRegisteredClient().getClientId());

			if (userPrincipal instanceof AuthUser authUser) {
				Set<String> roles = authUser.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
						.collect(Collectors.toCollection(LinkedHashSet::new));

				context.getClaims()
						.claim("email", authUser.getEmail())
						.claim("roles", roles)
						.claim("tenant_id", authUser.getTenantId())
						.audience(audience);
			}
			else {
				context.getClaims().audience(audience);
			}
		};
	}
}
