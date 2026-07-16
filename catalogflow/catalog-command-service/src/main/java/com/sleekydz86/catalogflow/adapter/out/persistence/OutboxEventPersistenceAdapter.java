package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.util.List;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import com.sleekydz86.catalogflow.application.port.out.OutboxEventPort;
import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

	private static final String AGGREGATE_TYPE = "Product";

	private final OutboxEventJpaRepository outboxEventJpaRepository;
	private final OutboxEventPayloadFactory outboxEventPayloadFactory;

	public OutboxEventPersistenceAdapter(
			OutboxEventJpaRepository outboxEventJpaRepository,
			OutboxEventPayloadFactory outboxEventPayloadFactory) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
		this.outboxEventPayloadFactory = outboxEventPayloadFactory;
	}

	@Override
	public void saveAll(List<DomainEvent> events) {
		for (DomainEvent event : events) {
			OutboxEventEntity entity = OutboxEventEntity.createEmpty();
			entity.setId(event.eventId());
			entity.setAggregateId(event.aggregateId().value());
			entity.setAggregateType(AGGREGATE_TYPE);
			entity.setEventType(event.eventType());
			entity.setAggregateVersion(event.aggregateVersion());
			entity.setPayload(outboxEventPayloadFactory.createPayload(event));
			entity.setCorrelationId(event.correlationId());
			entity.setCausationId(event.causationId());
			entity.setSchemaVersion(event.schemaVersion());
			entity.setPublished(false);
			entity.setCreatedAt(event.occurredAt());
			outboxEventJpaRepository.save(entity);
		}
	}
}
