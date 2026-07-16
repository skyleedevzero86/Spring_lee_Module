package com.sleekydz86.catalogflow.application.port.in;

import java.util.UUID;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;

public interface ApproveAiEnrichmentUseCase {

	ProductCommandResult approve(UUID productId, long expectedVersion);
}
