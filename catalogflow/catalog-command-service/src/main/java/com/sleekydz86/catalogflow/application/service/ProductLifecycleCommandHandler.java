package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.ProductLifecycleCommand;
import com.sleekydz86.catalogflow.application.port.in.ApproveAiEnrichmentUseCase;
import com.sleekydz86.catalogflow.application.port.in.PublishProductUseCase;
import com.sleekydz86.catalogflow.application.port.in.RequestAiEnrichmentUseCase;
import com.sleekydz86.catalogflow.application.port.in.SuspendProductUseCase;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.global.metrics.CatalogCommandMetrics;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductLifecycleCommandHandler implements
		RequestAiEnrichmentUseCase,
		ApproveAiEnrichmentUseCase,
		PublishProductUseCase,
		SuspendProductUseCase {

	private final ProductQuerySupport productQuerySupport;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final Clock clock;
	private final CatalogCommandMetrics catalogCommandMetrics;

	public ProductLifecycleCommandHandler(
			ProductQuerySupport productQuerySupport,
			ProductPersistenceCoordinator persistenceCoordinator,
			Clock clock,
			CatalogCommandMetrics catalogCommandMetrics) {
		this.productQuerySupport = productQuerySupport;
		this.persistenceCoordinator = persistenceCoordinator;
		this.clock = clock;
		this.catalogCommandMetrics = catalogCommandMetrics;
	}

	@Override
	public ProductCommandResult request(UUID productId, long expectedVersion) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(productId));
		Instant now = clock.instant();
		product.requestAiEnrichment(expectedVersion, now, CorrelationIdHolder.getOrGenerate());
		persistenceCoordinator.save(product);
		catalogCommandMetrics.incrementAiEnrichmentRequested();
		return ProductCommandResultMapper.toResult(product);
	}

	@Override
	public ProductCommandResult approve(UUID productId, long expectedVersion) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(productId));
		Instant now = clock.instant();
		product.approveAiEnrichment(expectedVersion, now, CorrelationIdHolder.getOrGenerate());
		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}

	@Override
	public ProductCommandResult publish(UUID productId, long expectedVersion) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(productId));
		Instant now = clock.instant();
		product.publish(expectedVersion, now, CorrelationIdHolder.getOrGenerate());
		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}

	@Override
	public ProductCommandResult suspend(ProductLifecycleCommand command) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(command.productId()));
		Instant now = clock.instant();
		product.suspend(command.expectedVersion(), command.reason(), now, CorrelationIdHolder.getOrGenerate());
		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}
}
