package com.sleekydz86.loginstudy.adminportal.web;

import java.time.Instant;
import java.util.List;

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
			String email,
			String displayName,
			String status,
			Instant joinedAt) {
	}
}
