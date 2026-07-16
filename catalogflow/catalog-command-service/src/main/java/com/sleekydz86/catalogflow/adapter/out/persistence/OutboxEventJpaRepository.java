package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
}
