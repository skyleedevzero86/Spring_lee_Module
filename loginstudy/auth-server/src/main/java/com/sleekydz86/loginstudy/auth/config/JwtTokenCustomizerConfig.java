package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.domain.UserRole;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class JwtTokenCustomizerConfig {

	public static final String AUDIENCE_MEMBER_SERVICE = "member-service";

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
			UserAccountRepository userAccountRepository) {
		return context -> {
			Authentication principal = context.getPrincipal();
			UserAccount account = userAccountRepository.findByUsername(principal.getName()).orElse(null);

			if (account != null) {
				Set<String> roles = account.getRoles().stream()
						.map(UserRole::getRole)
						.map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
						.collect(Collectors.toCollection(LinkedHashSet::new));

				context.getClaims()
						.claim("email", account.getEmail())
						.claim("roles", roles)
						.claim("tenant_id", account.getTenantId());
			}

			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				List<String> audience = new ArrayList<>(2);
				audience.add(AUDIENCE_MEMBER_SERVICE);
				audience.add(context.getRegisteredClient().getClientId());
				context.getClaims().audience(audience);
			}
		};
	}
}
