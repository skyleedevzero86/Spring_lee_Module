package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.time.Instant;

import com.sleekydz86.catalogflow.application.command.PresignedUploadUrlResult;

public record PresignedUploadUrlResponse(
		String storageKey,
		String uploadUrl,
		String downloadUrl,
		Instant expiresAt,
		String contentType,
		long sizeInBytes,
		boolean temporary) {

	public static PresignedUploadUrlResponse from(PresignedUploadUrlResult result) {
		return new PresignedUploadUrlResponse(
				result.storageKey(),
				result.uploadUrl(),
				result.downloadUrl(),
				result.expiresAt(),
				result.contentType(),
				result.sizeInBytes(),
				result.temporary());
	}
}
