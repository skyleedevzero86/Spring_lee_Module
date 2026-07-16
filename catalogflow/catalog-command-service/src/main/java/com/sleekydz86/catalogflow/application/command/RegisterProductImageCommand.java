package com.sleekydz86.catalogflow.application.command;

import java.util.UUID;

public record RegisterProductImageCommand(
		UUID productId,
		long expectedVersion,
		String storageKey,
		String contentType,
		long sizeInBytes,
		boolean temporary) {
}
