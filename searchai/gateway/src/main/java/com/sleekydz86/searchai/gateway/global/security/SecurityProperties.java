package com.sleekydz86.searchai.gateway.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
	String jwtSecret,
	long tokenTtlSeconds,
	List<DemoUser> users
) {

	public record DemoUser(String username, String password, String role) {
	}
}
