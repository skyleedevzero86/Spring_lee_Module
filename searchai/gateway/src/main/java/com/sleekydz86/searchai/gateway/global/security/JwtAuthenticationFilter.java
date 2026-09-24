package com.sleekydz86.searchai.gateway.global.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

public final class JwtAuthenticationFilter implements WebFilter {

	private final JwtTokenService jwtTokenService;

	public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
		this.jwtTokenService = jwtTokenService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String token = resolveToken(exchange);
		if (token == null || token.isBlank()) {
			return chain.filter(exchange);
		}
		return jwtTokenService.parse(token)
			.map(user -> {
				Authentication auth = new UsernamePasswordAuthenticationToken(
					user,
					token,
					List.of(new SimpleGrantedAuthority(user.role().authority()))
				);
				return chain.filter(exchange)
					.contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
			})
			.orElseGet(() -> {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			});
	}

	private static String resolveToken(ServerWebExchange exchange) {
		String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return exchange.getRequest().getQueryParams().getFirst("token");
	}
}
