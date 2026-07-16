package com.sleekydz86.catalogflow.application.command;

import java.util.UUID;

public record UpdateProductCommand(
		UUID productId,
		long expectedVersion,
		String name,
		String description,
		UUID categoryId,
		UUID supplierId) {
}
