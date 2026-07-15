package com.sleekydz86.loginstudy.member.config;

import com.sleekydz86.loginstudy.member.security.MemberJwtAuthenticationConverter;
import com.sleekydz86.loginstudy.member.web.MemberApiRateLimitFilter;
import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerSecurityConfig {

	@Bean
	FilterRegistrationBean<MemberApiRateLimitFilter> memberApiRateLimitFilterRegistration(
			MemberApiRateLimitFilter rateLimitFilter) {
		FilterRegistrationBean<MemberApiRateLimitFilter> registration = new FilterRegistrationBean<>(rateLimitFilter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, MemberApiRateLimitFilter rateLimitFilter)
			throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/actuator/health",
								"/actuator/info",
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html")
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/api/members/me").hasAuthority("SCOPE_member.read")
						.requestMatchers(HttpMethod.GET, "/api/members/**").hasAuthority("SCOPE_member.read")
						.requestMatchers(HttpMethod.PATCH, "/api/members/**").hasAuthority("SCOPE_member.write")
						.requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
						.anyRequest().authenticated())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(
								MemberJwtAuthenticationConverter.authenticationConverter())))
				.addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
				"http://localhost:8081",
				"http://localhost:8082"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
