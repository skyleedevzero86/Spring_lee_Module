package com.sleekydz86.catalogflow.application.command;

import java.util.UUID;

public record ProductLifecycleCommand(
		UUID productId,
		long expectedVersion,
		String reason) {
}
