package com.sleekydz86.catalogflow.adapter.out.messaging;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.OutboxEventJpaRepository;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {

	private final OutboxEventJpaRepository outboxEventJpaRepository;
	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties messagingProperties;
	private final OutboxRoutingKeyResolver routingKeyResolver;
	private final Clock clock;

	public OutboxPublisher(
			OutboxEventJpaRepository outboxEventJpaRepository,
			RabbitTemplate rabbitTemplate,
			MessagingProperties messagingProperties,
			OutboxRoutingKeyResolver routingKeyResolver,
			Clock clock) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
		this.rabbitTemplate = rabbitTemplate;
		this.messagingProperties = messagingProperties;
		this.routingKeyResolver = routingKeyResolver;
		this.clock = clock;
	}

	@Transactional
	public int publishPendingEvents(int batchSize) {
		List<OutboxEventEntity> events = outboxEventJpaRepository.findUnpublishedEvents();
		if (events.isEmpty()) {
			return 0;
		}
		List<OutboxEventEntity> target = events.stream().limit(batchSize).toList();
		for (OutboxEventEntity event : target) {
			send(event);
		}
		List<UUID> ids = target.stream().map(OutboxEventEntity::getId).toList();
		outboxEventJpaRepository.markPublished(ids, clock.instant());
		return ids.size();
	}

	private void send(OutboxEventEntity event) {
		try {
			MessageProperties properties = new MessageProperties();
			properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
			properties.setContentEncoding(StandardCharsets.UTF_8.name());
			properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
			properties.setMessageId(event.getId().toString());
			properties.setHeader("eventType", event.getEventType());
			properties.setHeader("aggregateId", event.getAggregateId().toString());
			properties.setHeader("correlationId", event.getCorrelationId());
			Message message = new Message(event.getPayload().getBytes(StandardCharsets.UTF_8), properties);
			rabbitTemplate.send(
					messagingProperties.getExchangeEvents(),
					routingKeyResolver.resolve(event.getEventType()),
					message);
		}
		catch (Exception exception) {
			throw new ApplicationException("아웃박스 이벤트 발행에 실패했습니다", exception);
		}
	}
}
