package com.sleekydz86.loginstudy.adminportal.config;

import com.sleekydz86.loginstudy.adminportal.security.OidcRolesAuthoritiesMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class AdminPortalSecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(AdminPortalSecurityConfig.class);

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			OAuth2AuthorizationRequestResolver authorizationRequestResolver,
			OidcRolesAuthoritiesMapper authoritiesMapper) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/",
								"/css/**",
								"/error",
								"/access-denied",
								"/actuator/health",
								"/actuator/info",
								"/actuator/prometheus",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html")
						.permitAll()
						.anyRequest()
						.hasRole("ADMIN"))
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/oauth2/authorization/admin-portal")
						.authorizationEndpoint(authorization -> authorization
								.authorizationRequestResolver(authorizationRequestResolver))
						.userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(authoritiesMapper))
						.defaultSuccessUrl("/admin", true)
						.failureHandler((request, response, exception) -> {
							log.warn("OAuth2 로그인 실패: {}", exception.getMessage(), exception);
							response.sendRedirect("/?loginFailed=1");
						}))
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
		return new OAuth2AuthorizationRequestResolver() {
			@Override
			public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
				return applyLoginPrompt(request, resolver.resolve(request));
			}

			@Override
			public OAuth2AuthorizationRequest resolve(
					HttpServletRequest request,
					String clientRegistrationId) {
				return applyLoginPrompt(request, resolver.resolve(request, clientRegistrationId));
			}
		};
	}

	private static OAuth2AuthorizationRequest applyLoginPrompt(
			HttpServletRequest request,
			OAuth2AuthorizationRequest authorizationRequest) {
		if (authorizationRequest == null || Boolean.parseBoolean(request.getParameter("remember"))) {
			return authorizationRequest;
		}
		Map<String, Object> parameters =
				new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
		parameters.put("prompt", "login");
		return OAuth2AuthorizationRequest.from(authorizationRequest)
				.additionalParameters(parameters)
				.build();
	}
}
