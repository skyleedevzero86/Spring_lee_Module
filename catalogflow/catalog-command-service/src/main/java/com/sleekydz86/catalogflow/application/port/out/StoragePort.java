package com.sleekydz86.catalogflow.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface StoragePort {

	PresignedUpload createPresignedUpload(
			UUID productId,
			String contentType,
			String fileExtension,
			boolean temporary);

	String createPresignedDownload(String storageKey);

	void deleteObject(String storageKey);

	boolean objectExists(String storageKey);

	record PresignedUpload(
			String storageKey,
			String uploadUrl,
			String downloadUrl,
			Instant expiresAt) {
	}
}
