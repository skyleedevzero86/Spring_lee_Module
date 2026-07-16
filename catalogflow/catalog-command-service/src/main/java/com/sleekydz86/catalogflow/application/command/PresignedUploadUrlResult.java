package com.sleekydz86.catalogflow.application.command;

import java.time.Instant;

public record PresignedUploadUrlResult(
		String storageKey,
		String uploadUrl,
		String downloadUrl,
		Instant expiresAt,
		String contentType,
		long sizeInBytes,
		boolean temporary) {
}
