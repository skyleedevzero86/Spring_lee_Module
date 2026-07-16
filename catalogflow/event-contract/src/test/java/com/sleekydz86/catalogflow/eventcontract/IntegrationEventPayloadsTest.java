package com.sleekydz86.catalogflow.eventcontract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class IntegrationEventPayloadsTest {

	@Test
	void shouldReadProductCreatedPayload() {
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T09:00:00Z");
		String json = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"ProductCreated\","
				+ "\"aggregateId\":\"" + UUID.randomUUID() + "\","
				+ "\"aggregateVersion\":0,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"name\":\"무선 키보드\","
				+ "\"description\":\"저소음 키보드\","
				+ "\"priceAmount\":59000,"
				+ "\"priceCurrency\":\"KRW\","
				+ "\"categoryId\":\"" + categoryId + "\","
				+ "\"supplierId\":\"" + supplierId + "\","
				+ "\"supplierName\":\"기본 공급사\","
				+ "\"status\":\"DRAFT\","
				+ "\"createdAt\":\"" + now + "\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		IntegrationEventPayloads.ProductCreatedData data = IntegrationEventPayloads.readProductCreated(json);
		assertEquals("무선 키보드", data.name());
		assertEquals(new BigDecimal("59000"), data.priceAmount());
		assertEquals(categoryId, data.categoryId());
		assertEquals("기본 공급사", data.supplierName());
	}
}
