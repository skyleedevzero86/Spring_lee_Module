package com.sleekydz86.catalogflow.adapter.out.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.OutboxEventJpaRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class OutboxPublisherIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private OutboxPublisher outboxPublisher;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@BeforeEach
	void stubPublisherConfirm() {
		doAnswer(invocation -> {
			CorrelationData correlationData = invocation.getArgument(3);
			correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
			return null;
		}).when(rabbitTemplate).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
	}

	@Test
	void shouldPublishAndMarkOutboxEvents() {
		OutboxEventEntity entity = OutboxEventEntity.createEmpty();
		entity.setId(UUID.randomUUID());
		entity.setAggregateId(UUID.randomUUID());
		entity.setAggregateType("Product");
		entity.setEventType("ProductCreated");
		entity.setAggregateVersion(0L);
		entity.setPayload("{\"eventType\":\"ProductCreated\"}");
		entity.setCorrelationId("corr-test");
		entity.setCausationId("");
		entity.setSchemaVersion(1);
		entity.setPublished(false);
		entity.setCreatedAt(Instant.now());
		outboxEventJpaRepository.save(entity);

		assertFalse(outboxEventJpaRepository.findUnpublishedEvents().isEmpty());
		int count = outboxPublisher.publishPendingEvents(100);
		assertTrue(count > 0);
		assertTrue(outboxEventJpaRepository.findUnpublishedEvents().isEmpty());
	}
}
