package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.time.Clock;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ConsumedEventEntity;
import com.sleekydz86.catalogflow.application.port.out.ConsumedEventStore;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConsumedEventStoreAdapter implements ConsumedEventStore {

	private final ConsumedEventJpaRepository consumedEventJpaRepository;
	private final Clock clock;

	public ConsumedEventStoreAdapter(ConsumedEventJpaRepository consumedEventJpaRepository, Clock clock) {
		this.consumedEventJpaRepository = consumedEventJpaRepository;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isConsumed(UUID eventId, String consumerName) {
		return consumedEventJpaRepository.existsByEventIdAndConsumerName(eventId, consumerName);
	}

	@Override
	@Transactional
	public void markConsumed(UUID eventId, String consumerName) {
		if (isConsumed(eventId, consumerName)) {
			return;
		}
		consumedEventJpaRepository.save(ConsumedEventEntity.create(eventId, consumerName, clock.instant()));
	}
}
