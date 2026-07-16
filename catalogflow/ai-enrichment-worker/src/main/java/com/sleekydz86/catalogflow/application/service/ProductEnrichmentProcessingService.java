package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.ai.EnrichmentResultValidator;
import com.sleekydz86.catalogflow.adapter.out.messaging.EnrichmentResultPublisher;
import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventPayloads;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;

@Service
public class ProductEnrichmentProcessingService {

	private final AiEnrichmentPort aiEnrichmentPort;
	private final EnrichmentResultValidator enrichmentResultValidator;
	private final EnrichmentResultPublisher enrichmentResultPublisher;
	private final Clock clock;

	public ProductEnrichmentProcessingService(
			AiEnrichmentPort aiEnrichmentPort,
			EnrichmentResultValidator enrichmentResultValidator,
			EnrichmentResultPublisher enrichmentResultPublisher,
			Clock clock) {
		this.aiEnrichmentPort = aiEnrichmentPort;
		this.enrichmentResultValidator = enrichmentResultValidator;
		this.enrichmentResultPublisher = enrichmentResultPublisher;
		this.clock = clock;
	}

	public void process(IntegrationEventEnvelope envelope) {
		if (!CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED.equals(envelope.eventType())) {
			throw new ApplicationException("지원하지 않는 AI 요청 이벤트입니다: " + envelope.eventType());
		}
		IntegrationEventPayloads.ProductEnrichmentRequestedData requestData =
				IntegrationEventPayloads.readProductEnrichmentRequested(envelope.payload());
		String correlationId = blankToGenerated(envelope.correlationId());
		CorrelationIdHolder.set(correlationId);
		try {
			AiEnrichmentPort.EnrichmentResult result = aiEnrichmentPort.enrich(new AiEnrichmentPort.EnrichmentRequest(
					envelope.aggregateId(),
					requestData.name(),
					requestData.description(),
					requestData.categoryId(),
					requestData.supplierName()));
			enrichmentResultValidator.validate(result);
			enrichmentResultPublisher.publishCompleted(
					envelope.aggregateId(),
					envelope.aggregateVersion(),
					correlationId,
					envelope.eventId().toString(),
					result,
					clock.instant());
		}
		catch (Exception exception) {
			enrichmentResultPublisher.publishFailed(
					envelope.aggregateId(),
					envelope.aggregateVersion(),
					correlationId,
					envelope.eventId().toString(),
					resolveFailureReason(exception),
					clock.instant());
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
