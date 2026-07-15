package com.sleekydz86.loginstudy.adminportal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@TestConfiguration(proxyBeanMethods = false)
class AdminPortalOAuth2TestConfig {

	@Bean
	@Primary
	ClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration registration = ClientRegistration.withRegistrationId("admin-portal")
				.clientId("admin-portal")
				.clientSecret("admin-portal-secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.scope("openid", "profile", "email", "member.read", "member.write", "admin")
				.authorizationUri("http://localhost:9000/oauth2/authorize")
				.tokenUri("http://localhost:9000/oauth2/token")
				.jwkSetUri("http://localhost:9000/oauth2/jwks")
				.userInfoUri("http://localhost:9000/userinfo")
				.userNameAttributeName("sub")
				.clientName("Admin Portal")
				.issuerUri("http://localhost:9000")
				.build();
		return new InMemoryClientRegistrationRepository(registration);
	}
}
