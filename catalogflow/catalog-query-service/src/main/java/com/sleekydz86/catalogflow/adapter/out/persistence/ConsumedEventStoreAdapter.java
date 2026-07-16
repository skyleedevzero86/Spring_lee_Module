package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.time.Clock;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.ConsumedEventStore;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class ConsumedEventStoreAdapter implements ConsumedEventStore {

	private final ConsumedEventMongoRepository consumedEventMongoRepository;
	private final Clock clock;

	public ConsumedEventStoreAdapter(ConsumedEventMongoRepository consumedEventMongoRepository, Clock clock) {
		this.consumedEventMongoRepository = consumedEventMongoRepository;
		this.clock = clock;
	}

	@Override
	public boolean isConsumed(UUID eventId, String consumerName) {
		return consumedEventMongoRepository.existsByEventIdAndConsumerName(eventId.toString(), consumerName);
	}

	@Override
	public void markConsumed(UUID eventId, String consumerName) {
		if (isConsumed(eventId, consumerName)) {
			return;
		}
		try {
			consumedEventMongoRepository.save(
					ConsumedEventDocument.create(eventId, consumerName, clock.instant()));
		}
		catch (DuplicateKeyException exception) {
			return;
		}
		catch (Exception exception) {
			throw new ApplicationException("소비 이벤트 기록에 실패했습니다", exception);
		}
	}
}
