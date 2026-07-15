package com.sleekydz86.loginstudy.auth.api;

import java.time.Instant;
import java.util.Set;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record UserAccountResponse(
			Long id,
			String username,
			String email,
			String tenantId,
			boolean enabled,
			boolean accountNonLocked,
			Set<String> roles,
			Instant createdAt) {
	}

	public record LoginHistoryResponse(
			Long id,
			String username,
			boolean success,
			String ipAddress,
			String failureReason,
			Instant createdAt) {
	}

	public record RegisteredClientSummaryResponse(
			String id,
			String clientId,
			String clientName,
			Set<String> grantTypes,
			Set<String> scopes) {
	}

	public record PersistenceHealthResponse(
			String registeredClientRepositoryType,
			String authorizationServiceType,
			String authorizationConsentServiceType,
			long userCount,
			long registeredClientCount,
			long authorizationCount,
			long consentCount,
			long loginHistoryCount) {
	}
}
