package com.sleekydz86.loginstudy.member.config;

import com.sleekydz86.loginstudy.member.security.MemberJwtAuthenticationConverter;
import com.sleekydz86.loginstudy.member.web.MemberApiRateLimitFilter;
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
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/members/me").hasAuthority("SCOPE_member.read")
						.requestMatchers(HttpMethod.GET, "/api/members/**").hasAuthority("SCOPE_member.read")
						.requestMatchers(HttpMethod.PATCH, "/api/members/**").hasAuthority("SCOPE_member.write")
						.requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(
								MemberJwtAuthenticationConverter.authenticationConverter())))
				.addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class);

		return http.build();
	}
}
