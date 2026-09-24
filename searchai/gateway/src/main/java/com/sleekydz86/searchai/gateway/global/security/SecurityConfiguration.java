package com.sleekydz86.searchai.gateway.global.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtTokenService jwtTokenService(SecurityProperties properties) {
		return new JwtTokenService(properties);
	}

	@Bean
	MapReactiveUserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder encoder) {
		List<UserDetails> users = properties.users().stream()
			.map(demo -> User.withUsername(demo.username())
				.password(encoder.encode(demo.password()))
				.roles(demo.role())
				.build())
			.map(UserDetails.class::cast)
			.toList();
		return new MapReactiveUserDetailsService(users);
	}

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
		return new JwtAuthenticationFilter(jwtTokenService);
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthenticationFilter jwtFilter) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.authorizeExchange(exchanges -> exchanges
				.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.pathMatchers("/auth/login", "/actuator/health", "/actuator/info").permitAll()
				.pathMatchers(HttpMethod.POST, "/rag/upload").hasRole("ADMIN")
				.pathMatchers("/admin/**").hasRole("ADMIN")
				.pathMatchers(HttpMethod.PUT, "/admin/**").hasRole("ADMIN")
				.pathMatchers("/chat/**", "/sse/**", "/auth/me").authenticated()
				.anyExchange().authenticated()
			)
			.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
			.build();
	}
}
