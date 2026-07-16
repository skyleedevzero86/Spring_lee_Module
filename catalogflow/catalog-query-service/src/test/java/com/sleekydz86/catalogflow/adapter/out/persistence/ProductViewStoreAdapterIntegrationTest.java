package com.sleekydz86.catalogflow.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.application.service.ProductViewProjectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest
@Testcontainers
class ProductViewStoreAdapterIntegrationTest {

	@Container
	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

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
				+ "\"name\":\"블루투스 마우스\","
				+ "\"description\":\"무소음 마우스\","
				+ "\"priceAmount\":39000,"
				+ "\"priceCurrency\":\"KRW\","
				+ "\"categoryId\":\"" + categoryId + "\","
				+ "\"supplierId\":\"" + supplierId + "\","
				+ "\"supplierName\":\"기본 공급사\","
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
		assertEquals("블루투스 마우스", productViewStoreAdapter.findByProductId(productId).orElseThrow().getName());
		assertEquals(new BigDecimal("39000"),
				productViewStoreAdapter.findByProductId(productId).orElseThrow().getPrice());
	}
}
