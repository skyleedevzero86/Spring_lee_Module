package com.sleekydz86.loginstudy.userportal.web;

import java.time.Instant;
import java.util.Set;

public record AuthProfileView(
		Long id,
		String username,
		String email,
		String displayName,
		String phone,
		String tenantId,
		boolean enabled,
		boolean accountNonLocked,
		String status,
		Set<String> roles,
		Instant createdAt) {
}
