package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.metrics.TokenEndpointMetricsFilter;
import com.sleekydz86.loginstudy.auth.security.AuthJwtAuthenticationConverter;
import com.sleekydz86.loginstudy.auth.security.LoginRememberSuccessHandler;
import com.sleekydz86.loginstudy.auth.security.PromptLoginReauthenticationFilter;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

	public static final String SCOPE_MEMBER_READ = "member.read";
	public static final String SCOPE_MEMBER_WRITE = "member.write";
	public static final String SCOPE_ADMIN = "admin";

	public static final String CLIENT_USER_PORTAL = "user-portal";
	public static final String CLIENT_ADMIN_PORTAL = "admin-portal";
	public static final String CLIENT_MEMBER_SERVICE = "member-service";

	@Bean
	FilterRegistrationBean<TokenEndpointMetricsFilter> tokenEndpointMetricsFilterRegistration(
			TokenEndpointMetricsFilter tokenEndpointMetricsFilter) {
		FilterRegistrationBean<TokenEndpointMetricsFilter> registration =
				new FilterRegistrationBean<>(tokenEndpointMetricsFilter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	@Order(1)
	SecurityFilterChain authorizationServerSecurityFilterChain(
			HttpSecurity http,
			TokenEndpointMetricsFilter tokenEndpointMetricsFilter) throws Exception {
		http
				.oauth2AuthorizationServer(authorizationServer -> {
					http.securityMatcher(authorizationServer.getEndpointsMatcher());
					authorizationServer
							.oidc(Customizer.withDefaults())
							.authorizationEndpoint(authorizationEndpoint ->
									authorizationEndpoint.consentPage("/oauth2/consent"));
				})
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
				.addFilterBefore(
						new PromptLoginReauthenticationFilter(),
						AnonymousAuthenticationFilter.class)
				.addFilterAfter(tokenEndpointMetricsFilter, AuthorizationFilter.class);

		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/login",
								"/register",
								"/register/**",
								"/account-recovery/**",
								"/error",
								"/css/**",
								"/actuator/health",
								"/actuator/info",
								"/actuator/prometheus",
								"/.well-known/**",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html")
						.permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**"))
				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.migrateSession()))
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(new LoginRememberSuccessHandler())
						.permitAll())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
						.defaultAuthenticationEntryPointFor(
								new BearerTokenAuthenticationEntryPoint(),
								request -> request.getServletPath().startsWith("/api/")))
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(
								AuthJwtAuthenticationConverter.authenticationConverter())))
				.logout(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
		return new JdbcRegisteredClientRepository(jdbcTemplate);
	}

	@Bean
	OAuth2AuthorizationConsentService authorizationConsentService(
			JdbcTemplate jdbcTemplate,
			RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}

	@Bean
	AuthorizationServerSettings authorizationServerSettings(
			@Value("${spring.security.oauth2.authorizationserver.issuer}") String issuer) {
		return AuthorizationServerSettings.builder()
				.issuer(issuer)
				.build();
	}

	static RegisteredClient userPortalClient(PasswordEncoder passwordEncoder, String clientSecret) {
		return RegisteredClient.withId(UUID.nameUUIDFromBytes(CLIENT_USER_PORTAL.getBytes()).toString())
				.clientId(CLIENT_USER_PORTAL)
				.clientSecret(passwordEncoder.encode(clientSecret))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://localhost:8081/login/oauth2/code/user-portal")
				.redirectUri("http://localhost:8080/user/login/oauth2/code/user-portal")
				.postLogoutRedirectUri("http://localhost:8081/")
				.postLogoutRedirectUri("http://localhost:8080/user/")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope(OidcScopes.EMAIL)
				.scope(SCOPE_MEMBER_READ)
				.scope(SCOPE_MEMBER_WRITE)
				.clientSettings(ClientSettings.builder()
						.requireAuthorizationConsent(true)
						.requireProofKey(true)
						.build())
				.tokenSettings(defaultTokenSettings())
				.build();
	}

	static RegisteredClient adminPortalClient(PasswordEncoder passwordEncoder, String clientSecret) {
		return RegisteredClient.withId(UUID.nameUUIDFromBytes(CLIENT_ADMIN_PORTAL.getBytes()).toString())
				.clientId(CLIENT_ADMIN_PORTAL)
				.clientSecret(passwordEncoder.encode(clientSecret))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://localhost:8082/login/oauth2/code/admin-portal")
				.redirectUri("http://localhost:8080/admin/login/oauth2/code/admin-portal")
				.postLogoutRedirectUri("http://localhost:8082/")
				.postLogoutRedirectUri("http://localhost:8080/admin/")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.scope(OidcScopes.EMAIL)
				.scope(SCOPE_MEMBER_READ)
				.scope(SCOPE_MEMBER_WRITE)
				.scope(SCOPE_ADMIN)
				.clientSettings(ClientSettings.builder()
						.requireAuthorizationConsent(true)
						.requireProofKey(true)
						.build())
				.tokenSettings(defaultTokenSettings())
				.build();
	}

	static RegisteredClient memberServiceClient(PasswordEncoder passwordEncoder, String clientSecret) {
		return RegisteredClient.withId(UUID.nameUUIDFromBytes(CLIENT_MEMBER_SERVICE.getBytes()).toString())
				.clientId(CLIENT_MEMBER_SERVICE)
				.clientSecret(passwordEncoder.encode(clientSecret))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.scope(SCOPE_MEMBER_READ)
				.scope(SCOPE_MEMBER_WRITE)
				.scope(SCOPE_ADMIN)
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(30))
						.build())
				.build();
	}

	private static TokenSettings defaultTokenSettings() {
		return TokenSettings.builder()
				.authorizationCodeTimeToLive(Duration.ofMinutes(5))
				.accessTokenTimeToLive(Duration.ofMinutes(15))
				.refreshTokenTimeToLive(Duration.ofHours(8))
				.reuseRefreshTokens(false)
				.build();
	}
}
