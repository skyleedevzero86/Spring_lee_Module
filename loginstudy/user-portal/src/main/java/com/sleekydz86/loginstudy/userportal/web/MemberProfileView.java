package com.sleekydz86.loginstudy.userportal.web;

import java.time.Instant;

public record MemberProfileView(
		Long id,
		String userSubject,
		String email,
		String displayName,
		String status,
		String tenantId,
		long version,
		Instant joinedAt,
		Instant updatedAt,
		Address address,
		Preferences preferences) {

	public record Address(
			String countryCode,
			String city,
			String streetLine,
			String postalCode) {
	}

	public record Preferences(
			boolean marketingOptIn,
			String locale,
			String timezone) {
	}
}
