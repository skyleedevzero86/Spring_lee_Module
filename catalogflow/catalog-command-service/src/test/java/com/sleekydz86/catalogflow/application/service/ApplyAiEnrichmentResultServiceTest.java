package com.sleekydz86.catalogflow.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.AiEnrichmentResultJpaRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.ProductJpaRepository;
import com.sleekydz86.catalogflow.domain.model.AiEnrichmentStatus;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class ApplyAiEnrichmentResultServiceTest {

	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private CreateProductCommandHandler createProductCommandHandler;

	@Autowired
	private ProductLifecycleCommandHandler productLifecycleCommandHandler;

	@Autowired
	private ApplyAiEnrichmentResultService applyAiEnrichmentResultService;

	@Autowired
	private ProductQuerySupport productQuerySupport;

	@Autowired
	private AiEnrichmentResultJpaRepository aiEnrichmentResultJpaRepository;

	@Autowired
	private ProductJpaRepository productJpaRepository;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@BeforeEach
	void setUp() {
		CorrelationIdHolder.clear();
	}

	@Test
	void shouldApplyCompletedEnrichmentResult() {
		var created = createProductCommandHandler.create(new com.sleekydz86.catalogflow.application.command.CreateProductCommand(
				"AI 테스트 상품",
				"원본 설명",
				java.math.BigDecimal.valueOf(10000),
				"KRW",
				CATEGORY_ID,
				SUPPLIER_ID));
		productLifecycleCommandHandler.request(created.productId(), created.version());

		UUID eventId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T12:00:00Z");
		String payload = """
				{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":1,"occurredAt":"%s","correlationId":"corr-ai","causationId":"","schemaVersion":1,"summary":"요약","generatedDescription":"생성설명","modelName":"stub-enrichment-v1","keywords":["무선","키보드"],"tags":["stub","ai"],"recommendedCategory":"%s","warnings":"검토필요","requiresHumanReview":true,"confidence":0.88,"promptVersion":"stub-prompt-v1","status":"REVIEW_REQUIRED","updatedAt":"%s"}
				""".formatted(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED,
				created.productId(),
				now,
				CATEGORY_ID,
				now).trim();

		applyAiEnrichmentResultService.apply(new IntegrationEventEnvelope(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED,
				created.productId(),
				1L,
				now,
				"corr-ai",
				"",
				1,
				payload));

		Product product = productQuerySupport.findProductOrThrow(new ProductId(created.productId()));
		assertEquals(AiEnrichmentStatus.COMPLETED, product.getAiEnrichmentStatus());
		assertTrue(product.getKeywords().stream().anyMatch(keyword -> keyword.value().equals("무선")));
		assertTrue(aiEnrichmentResultJpaRepository.findFirstByProductIdOrderByCreatedAtDesc(created.productId()).isPresent());
		assertTrue(productJpaRepository.findById(created.productId()).isPresent());

		applyAiEnrichmentResultService.apply(new IntegrationEventEnvelope(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED,
				created.productId(),
				1L,
				now,
				"corr-ai",
				"",
				1,
				payload));
		assertEquals(1, aiEnrichmentResultJpaRepository.findAll().stream()
				.filter(result -> result.getProductId().equals(created.productId()))
				.count());
	}
}
