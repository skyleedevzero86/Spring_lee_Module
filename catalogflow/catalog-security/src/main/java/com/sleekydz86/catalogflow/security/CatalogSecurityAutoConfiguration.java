package com.sleekydz86.catalogflow.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@ConditionalOnWebApplication
@EnableMethodSecurity
@EnableConfigurationProperties(CatalogSecurityProperties.class)
public class CatalogSecurityAutoConfiguration {

	@Bean
	RoleHierarchy catalogRoleHierarchy() {
		return RoleHierarchyImpl.fromHierarchy("""
				ROLE_SYSTEM_ADMIN > ROLE_CATALOG_MANAGER
				ROLE_CATALOG_MANAGER > ROLE_CATALOG_EDITOR
				ROLE_CATALOG_EDITOR > ROLE_CATALOG_VIEWER
				""");
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "false")
	SecurityFilterChain catalogSecurityDisabledFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	CatalogSecurityProblemHandlers catalogSecurityProblemHandlers(ObjectMapper objectMapper) {
		return new CatalogSecurityProblemHandlers(objectMapper);
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	JwtAuthenticationConverter catalogJwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new CatalogJwtGrantedAuthoritiesConverter());
		return converter;
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	SecurityFilterChain catalogSecurityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter catalogJwtAuthenticationConverter,
			CatalogSecurityProblemHandlers problemHandlers) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/system",
								"/api/v1/system/**",
								"/api/v1/system/docs",
								"/api/v1/system/docs/**",
								"/api/v1/system/api-docs",
								"/api/v1/system/api-docs/**",
								"/v3/api-docs",
								"/v3/api-docs/**",
								"/swagger-ui.html",
								"/swagger-ui/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_ADMIN,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers(HttpMethod.GET, "/api/v1/catalog/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_READ,
								CatalogRoles.ROLE_VIEWER,
								CatalogRoles.ROLE_EDITOR,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers(
								"/api/v1/products/*/publish",
								"/api/v1/products/*/suspend",
								"/api/v1/products/*/ai-enrichment/approve")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_PUBLISH,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_WRITE,
								CatalogRoles.ROLE_EDITOR,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers(HttpMethod.PUT, "/api/v1/products/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_WRITE,
								CatalogRoles.ROLE_EDITOR,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers(HttpMethod.PATCH, "/api/v1/products/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_WRITE,
								CatalogRoles.ROLE_EDITOR,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers("/api/v1/admin/**", "/api/v1/dead-letters/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_ADMIN,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.requestMatchers("/api/v1/batches/**")
						.hasAnyAuthority(
								CatalogScopes.SCOPE_BATCH_EXECUTE,
								CatalogRoles.ROLE_MANAGER,
								CatalogRoles.ROLE_SYSTEM_ADMIN)
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(catalogJwtAuthenticationConverter))
						.authenticationEntryPoint(problemHandlers)
						.accessDeniedHandler(problemHandlers))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(problemHandlers)
						.accessDeniedHandler(problemHandlers));
		return http.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(prefix = "app.security", name = "jwt-decoder-mode", havingValue = "issuer", matchIfMissing = true)
	JwtDecoder issuerJwtDecoder(CatalogSecurityProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(properties.getIssuerUri()).build();
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(properties.getIssuerUri());
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator(properties.getAudience())));
		return decoder;
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnProperty(prefix = "app.security", name = "jwt-decoder-mode", havingValue = "symmetric")
	JwtDecoder symmetricJwtDecoder(CatalogSecurityProperties properties) {
		byte[] secret = properties.getSymmetricSecret().getBytes(StandardCharsets.UTF_8);
		if (secret.length < 32) {
			byte[] padded = new byte[32];
			System.arraycopy(secret, 0, padded, 0, secret.length);
			secret = padded;
		}
		SecretKey key = new SecretKeySpec(secret, "HmacSHA256");
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefault(),
				audienceValidator(properties.getAudience())));
		return decoder;
	}

	private static OAuth2TokenValidator<Jwt> audienceValidator(String expectedAudience) {
		return jwt -> {
			if (expectedAudience == null || expectedAudience.isBlank()) {
				return OAuth2TokenValidatorResult.success();
			}
			if (jwt.getAudience() != null && jwt.getAudience().contains(expectedAudience)) {
				return OAuth2TokenValidatorResult.success();
			}
			OAuth2Error error = new OAuth2Error(
					"invalid_token",
					"JWT audience가 올바르지 않습니다",
					null);
			return OAuth2TokenValidatorResult.failure(error);
		};
	}
}
