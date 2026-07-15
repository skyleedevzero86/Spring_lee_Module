package com.sleekydz86.loginstudy.adminportal.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuth2ClientRegistrationConfig {

	@Bean
	@ConditionalOnMissingBean(ClientRegistrationRepository.class)
	ClientRegistrationRepository clientRegistrationRepository(
			OAuth2ClientProperties properties,
			@Value("${loginstudy.auth.issuer-uri}") String issuerUri) {
		properties.getProvider().values().forEach(provider -> provider.setIssuerUri(null));
		List<ClientRegistration> registrations = new OAuth2ClientPropertiesMapper(properties)
				.asClientRegistrations()
				.values()
				.stream()
				.map(registration -> ClientRegistration.withClientRegistration(registration)
						.issuerUri(issuerUri)
						.build())
				.toList();
		return new InMemoryClientRegistrationRepository(registrations);
	}
}
