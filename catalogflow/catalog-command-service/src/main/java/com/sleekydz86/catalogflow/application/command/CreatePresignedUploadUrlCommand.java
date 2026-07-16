package com.sleekydz86.catalogflow.application.command;

import java.util.UUID;

public record CreatePresignedUploadUrlCommand(
		UUID productId,
		String contentType,
		long sizeInBytes,
		String fileName,
		boolean temporary) {
}
