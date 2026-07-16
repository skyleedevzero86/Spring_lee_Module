package com.sleekydz86.catalogflow.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.ConsumedEventMongoRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.ProductViewMongoRepository;
import com.sleekydz86.catalogflow.application.service.ProductEventIngestionService;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.CatalogExchanges;
import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.messaging.consumer-enabled=true")
class ProductEventConsumerIntegrationTest {

	@Container
	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management-alpine");

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ConsumedEventMongoRepository consumedEventMongoRepository;

	@Autowired
	private ProductViewMongoRepository productViewMongoRepository;

	@Test
	void shouldConsumeProductCreatedEventWithIdempotency() throws InterruptedException {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		Instant now = Instant.now();
		String payload = "{"
				+ "\"eventId\":\"" + eventId + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_CREATED + "\","
				+ "\"aggregateId\":\"" + aggregateId + "\","
				+ "\"aggregateVersion\":0,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-it\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
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

		MessageProperties properties = new MessageProperties();
		properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
		properties.setMessageId(eventId.toString());
		properties.setHeader(MessagingHeaders.EVENT_TYPE, CatalogEventTypes.PRODUCT_CREATED);
		properties.setHeader(MessagingHeaders.AGGREGATE_ID, aggregateId.toString());
		properties.setHeader(MessagingHeaders.CORRELATION_ID, "corr-it");
		properties.setHeader(MessagingHeaders.TRACE_ID, "trace-it");
		Message message = new Message(payload.getBytes(), properties);

		rabbitTemplate.send(
				CatalogExchanges.EVENTS,
				CatalogRoutingKeys.resolve(CatalogEventTypes.PRODUCT_CREATED),
				message);
		rabbitTemplate.send(
				CatalogExchanges.EVENTS,
				CatalogRoutingKeys.resolve(CatalogEventTypes.PRODUCT_CREATED),
				message);

		assertTrue(waitUntilConsumed(eventId));
		assertTrue(waitUntilProductViewCreated(aggregateId));
		var productView = productViewMongoRepository.findById(aggregateId.toString()).orElseThrow();
		assertEquals("무선 키보드", productView.getName());
		assertEquals(new BigDecimal("59000"), productView.getPrice());
		assertEquals("DRAFT", productView.getStatus());
	}

	private boolean waitUntilConsumed(UUID eventId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
		while (System.currentTimeMillis() < deadline) {
			if (consumedEventMongoRepository.existsByEventIdAndConsumerName(
					eventId.toString(),
					ProductEventIngestionService.CONSUMER_NAME)) {
				return true;
			}
			Thread.sleep(200);
		}
		return consumedEventMongoRepository.existsByEventIdAndConsumerName(
				eventId.toString(),
				ProductEventIngestionService.CONSUMER_NAME);
	}

	private boolean waitUntilProductViewCreated(UUID aggregateId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis();
		while (System.currentTimeMillis() < deadline) {
			if (productViewMongoRepository.findById(aggregateId.toString()).isPresent()) {
				return true;
			}
			Thread.sleep(200);
		}
		return productViewMongoRepository.findById(aggregateId.toString()).isPresent();
	}
}
