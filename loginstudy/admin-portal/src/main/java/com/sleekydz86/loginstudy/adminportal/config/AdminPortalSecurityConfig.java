package com.sleekydz86.loginstudy.adminportal.config;

import com.sleekydz86.loginstudy.adminportal.security.OidcRolesAuthoritiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AdminPortalSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			OAuth2AuthorizationRequestResolver authorizationRequestResolver,
			OidcRolesAuthoritiesMapper authoritiesMapper) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/css/**", "/error", "/access-denied",
								"/actuator/health", "/actuator/info")
						.permitAll()
						.anyRequest()
						.hasRole("ADMIN"))
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/oauth2/authorization/admin-portal")
						.authorizationEndpoint(authorization -> authorization
								.authorizationRequestResolver(authorizationRequestResolver))
						.userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(authoritiesMapper))
						.defaultSuccessUrl("/admin", true))
				.exceptionHandling(exceptions -> exceptions
						.accessDeniedPage("/access-denied"))
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("ADMINSESSION"))
				.csrf(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	OAuth2AuthorizationRequestResolver authorizationRequestResolver(
			ClientRegistrationRepository clientRegistrationRepository) {
		DefaultOAuth2AuthorizationRequestResolver resolver =
				new DefaultOAuth2AuthorizationRequestResolver(
						clientRegistrationRepository,
						"/oauth2/authorization");
		resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
		return resolver;
	}
}
