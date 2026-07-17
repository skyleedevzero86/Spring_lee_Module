package com.sleekydz86.loginstudy.member.api;

import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class MemberDtos {

	private MemberDtos() {
	}

	public record AddressResponse(
			String countryCode,
			String city,
			String streetLine,
			String postalCode) {
	}

	public record PreferencesResponse(
			boolean marketingOptIn,
			String locale,
			String timezone) {
	}

	public record MemberResponse(
			Long id,
			String userSubject,
			String email,
			String displayName,
			MemberStatus status,
			String tenantId,
			long version,
			Instant joinedAt,
			Instant updatedAt,
			AddressResponse address,
			PreferencesResponse preferences) {
	}

	public record MemberSummaryResponse(
			Long id,
			String userSubject,
			String email,
			String displayName,
			MemberStatus status,
			Instant joinedAt) {
	}

	public enum SensitiveField {
		EMAIL,
		DISPLAY_NAME
	}

	public record SensitiveFieldResponse(
			Long memberId,
			SensitiveField field,
			String value) {
	}

	public record UpdateMemberRequest(
			@NotNull Long version,
			@NotBlank @Size(max = 100) String displayName,
			@Email @Size(max = 255) String email,
			AddressRequest address,
			PreferencesRequest preferences) {
	}

	public record AddressRequest(
			@NotBlank @Size(min = 2, max = 2) String countryCode,
			@NotBlank @Size(max = 100) String city,
			@NotBlank @Size(max = 255) String streetLine,
			@NotBlank @Size(max = 32) String postalCode) {
	}

	public record PreferencesRequest(
			boolean marketingOptIn,
			@NotBlank @Size(max = 16) String locale,
			@NotBlank @Size(max = 64) String timezone) {
	}

	public record ChangeStatusRequest(
			@NotNull MemberStatus status,
			@Size(max = 255) String reason) {
	}

	public record PageResponse<T>(
			List<T> content,
			int page,
			int size,
			long totalElements,
			int totalPages) {
	}

	public record KeysetPageResponse(
			List<MemberSummaryResponse> content,
			boolean hasNext,
			Long nextCursorId,
			Instant nextCursorJoinedAt) {
	}
}
