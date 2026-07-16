package com.sleekydz86.catalogflow.application.port.in;

import java.util.UUID;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.ProductLifecycleCommand;

public interface RequestAiEnrichmentUseCase {

	ProductCommandResult request(UUID productId, long expectedVersion);
}
