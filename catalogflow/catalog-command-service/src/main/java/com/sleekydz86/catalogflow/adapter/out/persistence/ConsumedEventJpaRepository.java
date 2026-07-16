package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ConsumedEventEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ConsumedEventEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsumedEventJpaRepository extends JpaRepository<ConsumedEventEntity, ConsumedEventEntityId> {

	boolean existsByEventIdAndConsumerName(UUID eventId, String consumerName);
}
