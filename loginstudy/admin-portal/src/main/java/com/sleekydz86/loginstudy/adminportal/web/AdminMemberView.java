package com.sleekydz86.loginstudy.adminportal.web;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class AdminMemberView {

	private AdminMemberView() {
	}

	public record Page(
			List<Member> content,
			int page,
			int size,
			long totalElements,
			int totalPages) {

		public Page {
			content = content == null ? List.of() : List.copyOf(content);
		}

		public static Page empty() {
			return new Page(List.of(), 0, 20, 0, 0);
		}
	}

	public record Member(
			Long id,
			String userSubject,
			String email,
			String displayName,
			String status,
			Instant joinedAt,
			String role,
			String accountStatus) {

		public String effectiveStatus() {
			return accountStatus == null || accountStatus.isBlank()
					? status
					: accountStatus;
		}

		public Member withAccount(AuthUser account) {
			String primaryRole = account.roles() != null && account.roles().contains("ADMIN")
					? "ADMIN"
					: "USER";
			return new Member(
					id,
					userSubject,
					email,
					displayName,
					status,
					joinedAt,
					primaryRole,
					account.status());
		}
	}

	public record AuthUser(
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
}
