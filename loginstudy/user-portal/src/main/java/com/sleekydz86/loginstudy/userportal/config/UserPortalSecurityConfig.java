package com.sleekydz86.loginstudy.userportal.config;

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
public class UserPortalSecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/css/**", "/error", "/actuator/health", "/actuator/info")
						.permitAll()
						.anyRequest()
						.authenticated())
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/oauth2/authorization/user-portal")
						.authorizationEndpoint(authorization -> authorization
								.authorizationRequestResolver(authorizationRequestResolver))
						.defaultSuccessUrl("/home", true))
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("USERSESSION"))
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
