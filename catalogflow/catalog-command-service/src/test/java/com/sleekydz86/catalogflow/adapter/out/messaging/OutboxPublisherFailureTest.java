package com.sleekydz86.catalogflow.adapter.out.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.OutboxEventJpaRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
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
class OutboxPublisherFailureTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private OutboxEventJpaRepository outboxEventJpaRepository;

	@Autowired
	private OutboxPublisher outboxPublisher;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@Test
	@DisplayName("RabbitMQ 발행 실패 시 Outbox는 미발행 상태로 남는다")
	void shouldKeepUnpublishedWhenRabbitPublishFails() {
		// given
		OutboxEventEntity entity = OutboxEventEntity.createEmpty();
		entity.setId(UUID.randomUUID());
		entity.setAggregateId(UUID.randomUUID());
		entity.setAggregateType("Product");
		entity.setEventType("ProductCreated");
		entity.setAggregateVersion(0L);
		entity.setPayload("{\"eventType\":\"ProductCreated\"}");
		entity.setCorrelationId("corr-fail");
		entity.setCausationId("");
		entity.setSchemaVersion(1);
		entity.setPublished(false);
		entity.setCreatedAt(Instant.now());
		outboxEventJpaRepository.save(entity);
		doThrow(new RuntimeException("broker unavailable"))
				.when(rabbitTemplate)
				.send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));

		// when / then
		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> outboxPublisher.publishPendingEvents(100));
		assertTrue(exception.getMessage().contains("아웃박스 이벤트 발행에 실패했습니다"));
		assertFalse(outboxEventJpaRepository.findUnpublishedEvents().isEmpty());
	}
}
