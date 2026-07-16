package com.sleekydz86.catalogflow.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumedEventMongoRepository extends MongoRepository<ConsumedEventDocument, String> {

	boolean existsByEventIdAndConsumerName(String eventId, String consumerName);
}
