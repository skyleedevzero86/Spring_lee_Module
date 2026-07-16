package com.sleekydz86.catalogflow.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sleekydz86.catalogflow.adapter.out.persistence.AiEnrichmentResultJpaRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.AiEnrichmentResultEntity;
import com.sleekydz86.catalogflow.application.port.out.ConsumedEventStore;
import com.sleekydz86.catalogflow.domain.model.AiEnrichmentStatus;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductKeyword;
import com.sleekydz86.catalogflow.domain.model.ProductTag;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventPayloads;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplyAiEnrichmentResultService {

	public static final String CONSUMER_NAME = "catalog-command-service";

	private final ConsumedEventStore consumedEventStore;
	private final ProductQuerySupport productQuerySupport;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final AiEnrichmentResultJpaRepository aiEnrichmentResultJpaRepository;
	private final Clock clock;

	public ApplyAiEnrichmentResultService(
			ConsumedEventStore consumedEventStore,
			ProductQuerySupport productQuerySupport,
			ProductPersistenceCoordinator persistenceCoordinator,
			AiEnrichmentResultJpaRepository aiEnrichmentResultJpaRepository,
			Clock clock) {
		this.consumedEventStore = consumedEventStore;
		this.productQuerySupport = productQuerySupport;
		this.persistenceCoordinator = persistenceCoordinator;
		this.aiEnrichmentResultJpaRepository = aiEnrichmentResultJpaRepository;
		this.clock = clock;
	}

	@Transactional
	public void apply(IntegrationEventEnvelope envelope) {
		if (consumedEventStore.isConsumed(envelope.eventId(), CONSUMER_NAME)) {
			return;
		}
		if (CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED.equals(envelope.eventType())) {
			applyCompleted(envelope);
		}
		else if (CatalogEventTypes.PRODUCT_ENRICHMENT_FAILED.equals(envelope.eventType())) {
			applyFailed(envelope);
		}
		else {
			throw new ApplicationException("지원하지 않는 AI 결과 이벤트입니다: " + envelope.eventType());
		}
		consumedEventStore.markConsumed(envelope.eventId(), CONSUMER_NAME);
	}

	private void applyCompleted(IntegrationEventEnvelope envelope) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(envelope.aggregateId()));
		if (product.getAiEnrichmentStatus() == AiEnrichmentStatus.COMPLETED
				|| product.getAiEnrichmentStatus() == AiEnrichmentStatus.FAILED) {
			return;
		}
		IntegrationEventPayloads.ProductEnrichmentCompletedData data =
				IntegrationEventPayloads.readProductEnrichmentCompleted(envelope.payload());
		String correlationId = blankToGenerated(envelope.correlationId());
		saveEnrichmentResult(envelope.aggregateId(), data);
		if (product.getAiEnrichmentStatus() == AiEnrichmentStatus.REQUESTED) {
			product.markAiEnrichmentProcessing(product.getVersion(), clock.instant(), correlationId);
		}
		Set<ProductKeyword> keywords = data.keywords().stream()
				.filter(value -> value != null && !value.isBlank())
				.map(ProductKeyword::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Set<ProductTag> tags = data.tags().stream()
				.filter(value -> value != null && !value.isBlank())
				.map(ProductTag::new)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (keywords.isEmpty()) {
			keywords.add(new ProductKeyword("상품"));
		}
		if (tags.isEmpty()) {
			tags.add(new ProductTag("ai"));
		}
		product.completeAiEnrichment(
				product.getVersion(),
				data.modelName(),
				keywords,
				tags,
				clock.instant(),
				correlationId);
		persistenceCoordinator.save(product);
	}

	private void applyFailed(IntegrationEventEnvelope envelope) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(envelope.aggregateId()));
		if (product.getAiEnrichmentStatus() == AiEnrichmentStatus.COMPLETED
				|| product.getAiEnrichmentStatus() == AiEnrichmentStatus.FAILED
				|| product.getAiEnrichmentStatus() == AiEnrichmentStatus.NOT_REQUESTED) {
			return;
		}
		IntegrationEventPayloads.ProductEnrichmentFailedData data =
				IntegrationEventPayloads.readProductEnrichmentFailed(envelope.payload());
		product.failAiEnrichment(
				product.getVersion(),
				data.reason(),
				clock.instant(),
				blankToGenerated(envelope.correlationId()));
		persistenceCoordinator.save(product);
	}

	private void saveEnrichmentResult(UUID productId, IntegrationEventPayloads.ProductEnrichmentCompletedData data) {
		AiEnrichmentResultEntity entity = new AiEnrichmentResultEntity();
		entity.setId(UUID.randomUUID());
		entity.setProductId(productId);
		entity.setModelName(data.modelName() == null || data.modelName().isBlank() ? "unknown" : data.modelName());
		entity.setSummary(data.summary());
		entity.setGeneratedDescription(data.generatedDescription());
		entity.setRecommendedCategory(data.recommendedCategory());
		entity.setWarnings(data.warnings());
		entity.setRequiresHumanReview(data.requiresHumanReview());
		entity.setConfidence(BigDecimal.valueOf(data.confidence()).setScale(4, RoundingMode.HALF_UP));
		entity.setPromptVersion(data.promptVersion());
		entity.setCreatedAt(clock.instant());
		entity.setUpdatedAt(clock.instant());
		aiEnrichmentResultJpaRepository.save(entity);
	}

	private String blankToGenerated(String correlationId) {
		if (correlationId == null || correlationId.isBlank()) {
			return CorrelationIdHolder.getOrGenerate();
		}
		return correlationId;
	}
}
