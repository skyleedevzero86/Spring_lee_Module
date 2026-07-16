package com.sleekydz86.catalogflow.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.ai.EnrichmentPromptTemplate;
import com.sleekydz86.catalogflow.adapter.out.ai.EnrichmentResultValidator;
import com.sleekydz86.catalogflow.adapter.out.ai.StubEnrichmentEngine;
import com.sleekydz86.catalogflow.adapter.out.messaging.EnrichmentResultPublisher;
import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import com.sleekydz86.catalogflow.global.metrics.CatalogAiMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductEnrichmentFailureTest {

	private static final Instant NOW = Instant.parse("2026-07-16T12:00:00Z");

	private AiEnrichmentPort aiEnrichmentPort;
	private EnrichmentResultPublisher enrichmentResultPublisher;
	private AiProperties aiProperties;
	private ProductEnrichmentProcessingService service;

	@BeforeEach
	void setUp() {
		aiEnrichmentPort = mock(AiEnrichmentPort.class);
		enrichmentResultPublisher = mock(EnrichmentResultPublisher.class);
		aiProperties = new AiProperties();
		aiProperties.setProvider("ollama");
		aiProperties.setFallbackEnabled(false);
		aiProperties.setModelName("stub-enrichment-v1");
		aiProperties.setPromptVersion("stub-prompt-v1");
		service = new ProductEnrichmentProcessingService(
				aiEnrichmentPort,
				new StubEnrichmentEngine(aiProperties, new EnrichmentPromptTemplate()),
				new EnrichmentResultValidator(),
				enrichmentResultPublisher,
				aiProperties,
				Clock.fixed(NOW, ZoneOffset.UTC),
				new CatalogAiMetrics(new SimpleMeterRegistry()));
	}

	@Test
	@DisplayName("AI Provider 실패 시 실패 이벤트를 발행한다")
	void shouldPublishFailedWhenProviderFails() {
		// given
		when(aiEnrichmentPort.enrich(any())).thenThrow(new RuntimeException("Ollama 연결 실패"));
		IntegrationEventEnvelope envelope = enrichmentRequestedEnvelope();

		// when
		service.process(envelope);

		// then
		verify(enrichmentResultPublisher).publishFailed(
				eq(envelope.aggregateId()),
				eq(1L),
				eq("corr-ai-fail"),
				eq(envelope.eventId().toString()),
				eq("Ollama 연결 실패"),
				eq(NOW));
		verify(enrichmentResultPublisher, never()).publishCompleted(
				any(), anyLong(), anyString(), anyString(), any(), any());
	}

	@Test
	@DisplayName("AI Provider 실패 시 Stub 폴백이 켜져 있으면 성공 이벤트를 발행한다")
	void shouldFallbackToStubWhenEnabled() {
		// given
		aiProperties.setFallbackEnabled(true);
		when(aiEnrichmentPort.enrich(any())).thenThrow(new RuntimeException("Gemini Rate Limit"));
		IntegrationEventEnvelope envelope = enrichmentRequestedEnvelope();

		// when
		service.process(envelope);

		// then
		verify(enrichmentResultPublisher).publishCompleted(
				eq(envelope.aggregateId()),
				eq(1L),
				eq("corr-ai-fail"),
				eq(envelope.eventId().toString()),
				any(AiEnrichmentPort.EnrichmentResult.class),
				eq(NOW));
		verify(enrichmentResultPublisher, never()).publishFailed(
				any(), anyLong(), anyString(), anyString(), anyString(), any());
	}

	@Test
	@DisplayName("잘못된 AI JSON은 검증 실패로 실패 이벤트를 발행한다")
	void shouldPublishFailedWhenAiJsonInvalid() {
		// given
		when(aiEnrichmentPort.enrich(any())).thenReturn(new AiEnrichmentPort.EnrichmentResult(
				"broken-model",
				"",
				"",
				List.of(),
				List.of(),
				"",
				"",
				false,
				0.0,
				"v1"));
		IntegrationEventEnvelope envelope = enrichmentRequestedEnvelope();

		// when
		service.process(envelope);

		// then
		verify(enrichmentResultPublisher).publishFailed(
				eq(envelope.aggregateId()),
				eq(1L),
				eq("corr-ai-fail"),
				eq(envelope.eventId().toString()),
				anyString(),
				eq(NOW));
	}

	private IntegrationEventEnvelope enrichmentRequestedEnvelope() {
		UUID eventId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		String payload = """
				{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":1,"occurredAt":"%s","correlationId":"corr-ai-fail","causationId":"","schemaVersion":1,"name":"테스트상품","description":"설명","categoryId":"%s","supplierId":"%s","supplierName":"공급사","status":"ENRICHMENT_PENDING","updatedAt":"%s"}
				""".formatted(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED,
				productId,
				NOW,
				categoryId,
				supplierId,
				NOW).trim();
		return new IntegrationEventEnvelope(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED,
				productId,
				1L,
				NOW,
				"corr-ai-fail",
				"",
				1,
				payload);
	}
}
