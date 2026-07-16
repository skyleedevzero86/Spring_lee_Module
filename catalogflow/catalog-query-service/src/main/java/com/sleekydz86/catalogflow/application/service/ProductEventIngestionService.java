package com.sleekydz86.catalogflow.application.service;

import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.ConsumedEventStore;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.stereotype.Service;

@Service
public class ProductEventIngestionService {

	public static final String CONSUMER_NAME = "catalog-query-service";

	private final ConsumedEventStore consumedEventStore;

	public ProductEventIngestionService(ConsumedEventStore consumedEventStore) {
		this.consumedEventStore = consumedEventStore;
	}

	public void ingest(IntegrationEventEnvelope envelope) {
		if (!CatalogEventTypes.QUERY_PRODUCT_EVENT_TYPES.contains(envelope.eventType())) {
			throw new ApplicationException("지원하지 않는 상품 이벤트 유형입니다: " + envelope.eventType());
		}
		if (consumedEventStore.isConsumed(envelope.eventId(), CONSUMER_NAME)) {
			return;
		}
		consumedEventStore.markConsumed(envelope.eventId(), CONSUMER_NAME);
	}
}
