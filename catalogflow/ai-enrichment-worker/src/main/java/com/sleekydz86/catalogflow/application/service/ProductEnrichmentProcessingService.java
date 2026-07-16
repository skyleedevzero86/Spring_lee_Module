package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.ai.EnrichmentResultValidator;
import com.sleekydz86.catalogflow.adapter.out.ai.StubEnrichmentEngine;
import com.sleekydz86.catalogflow.adapter.out.messaging.EnrichmentResultPublisher;
import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventPayloads;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.global.metrics.CatalogAiMetrics;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class ProductEnrichmentProcessingService {

	private final AiEnrichmentPort aiEnrichmentPort;
	private final StubEnrichmentEngine stubEnrichmentEngine;
	private final EnrichmentResultValidator enrichmentResultValidator;
	private final EnrichmentResultPublisher enrichmentResultPublisher;
	private final AiProperties aiProperties;
	private final Clock clock;
	private final CatalogAiMetrics catalogAiMetrics;

	public ProductEnrichmentProcessingService(
			AiEnrichmentPort aiEnrichmentPort,
			StubEnrichmentEngine stubEnrichmentEngine,
			EnrichmentResultValidator enrichmentResultValidator,
			EnrichmentResultPublisher enrichmentResultPublisher,
			AiProperties aiProperties,
			Clock clock,
			CatalogAiMetrics catalogAiMetrics) {
		this.aiEnrichmentPort = aiEnrichmentPort;
		this.stubEnrichmentEngine = stubEnrichmentEngine;
		this.enrichmentResultValidator = enrichmentResultValidator;
		this.enrichmentResultPublisher = enrichmentResultPublisher;
		this.aiProperties = aiProperties;
		this.clock = clock;
		this.catalogAiMetrics = catalogAiMetrics;
	}

	public void process(IntegrationEventEnvelope envelope) {
		if (!CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED.equals(envelope.eventType())) {
			throw new ApplicationException("지원하지 않는 AI 요청 이벤트입니다: " + envelope.eventType());
		}
		catalogAiMetrics.incrementRequested();
		Timer.Sample sample = catalogAiMetrics.startTimer();
		IntegrationEventPayloads.ProductEnrichmentRequestedData requestData =
				IntegrationEventPayloads.readProductEnrichmentRequested(envelope.payload());
		String correlationId = blankToGenerated(envelope.correlationId());
		CorrelationIdHolder.set(correlationId);
		AiEnrichmentPort.EnrichmentRequest request = new AiEnrichmentPort.EnrichmentRequest(
				envelope.aggregateId(),
				requestData.name(),
				requestData.description(),
				requestData.categoryId(),
				requestData.supplierName());
		try {
			AiEnrichmentPort.EnrichmentResult result = enrichWithFallback(request);
			enrichmentResultValidator.validate(result);
			enrichmentResultPublisher.publishCompleted(
					envelope.aggregateId(),
					envelope.aggregateVersion(),
					correlationId,
					envelope.eventId().toString(),
					result,
					clock.instant());
			catalogAiMetrics.incrementCompleted();
		}
		catch (Exception exception) {
			catalogAiMetrics.incrementFailed();
			enrichmentResultPublisher.publishFailed(
					envelope.aggregateId(),
					envelope.aggregateVersion(),
					correlationId,
					envelope.eventId().toString(),
					resolveFailureReason(exception),
					clock.instant());
		}
		finally {
			catalogAiMetrics.record(sample);
		}
	}

	private AiEnrichmentPort.EnrichmentResult enrichWithFallback(AiEnrichmentPort.EnrichmentRequest request) {
		try {
			return aiEnrichmentPort.enrich(request);
		}
		catch (Exception exception) {
			if (!aiProperties.isFallbackEnabled() || "stub".equalsIgnoreCase(aiProperties.getProvider())) {
				throw exception;
			}
			return stubEnrichmentEngine.enrich(request);
		}
	}

	private String resolveFailureReason(Exception exception) {
		String message = exception.getMessage();
		if (message == null || message.isBlank()) {
			return "AI 가공 중 알 수 없는 오류가 발생했습니다";
		}
		return message;
	}

	private String blankToGenerated(String correlationId) {
		if (correlationId == null || correlationId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return correlationId;
	}
}
