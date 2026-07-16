package com.sleekydz86.catalogflow.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.service.ProductViewProjectionService;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class ProductViewStoreAdapterIntegrationTest {

	@Container
	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management-alpine");

	@Container
	@ServiceConnection(name = "redis")
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

	@Autowired
	private ProductViewStoreAdapter productViewStoreAdapter;

	@Autowired
	private ProductViewProjectionService productViewProjectionService;

	@Test
	void shouldPersistProductViewInMongoDb() {
		UUID productId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T10:00:00Z");
		String payload = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_CREATED + "\","
				+ "\"aggregateId\":\"" + productId + "\","
				+ "\"aggregateVersion\":0,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-mongo\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
				+ "\"name\":\"\uBE14\uB8E8\uD22C\uC2A4 \uB9C8\uC6B0\uC2A4\","
				+ "\"description\":\"\uBB34\uC18C\uC74C \uB9C8\uC6B0\uC2A4\","
				+ "\"priceAmount\":39000,"
				+ "\"priceCurrency\":\"KRW\","
				+ "\"categoryId\":\"" + categoryId + "\","
				+ "\"supplierId\":\"" + supplierId + "\","
				+ "\"supplierName\":\"\uAE30\uBCF8 \uACF5\uAE09\uC0AC\","
				+ "\"status\":\"DRAFT\","
				+ "\"createdAt\":\"" + now + "\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		productViewProjectionService.project(new IntegrationEventEnvelope(
				UUID.randomUUID(),
				CatalogEventTypes.PRODUCT_CREATED,
				productId,
				0L,
				now,
				"corr-mongo",
				"",
				1,
				payload));

		assertTrue(productViewStoreAdapter.findByProductId(productId).isPresent());
		assertEquals(
				"\uBE14\uB8E8\uD22C\uC2A4 \uB9C8\uC6B0\uC2A4",
				productViewStoreAdapter.findByProductId(productId).orElseThrow().getName());
		assertEquals(new BigDecimal("39000"),
				productViewStoreAdapter.findByProductId(productId).orElseThrow().getPrice());
	}
}
