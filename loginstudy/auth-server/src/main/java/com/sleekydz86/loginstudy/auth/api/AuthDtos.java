package com.sleekydz86.loginstudy.auth.api;

import com.sleekydz86.loginstudy.auth.domain.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record UserAccountResponse(
			Long id,
			String username,
			String email,
			String displayName,
			String phone,
			String tenantId,
			boolean enabled,
			boolean accountNonLocked,
			AccountStatus status,
			Set<String> roles,
			Instant createdAt) {
	}

	public record ChangeRoleRequest(
			@NotBlank @Pattern(regexp = "USER|ADMIN") String role) {
	}

	public record ChangeAccountStatusRequest(
			@NotNull AccountStatus status) {
	}

	public record UpdateOwnProfileRequest(
			@NotBlank @Size(max = 100) String displayName,
			@NotBlank @Email @Size(max = 255) String email,
			@NotBlank @Pattern(regexp = "^[0-9+() -]{7,30}$") String phone) {
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
