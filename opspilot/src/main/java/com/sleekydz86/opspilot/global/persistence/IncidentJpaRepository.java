package com.sleekydz86.opspilot.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IncidentJpaRepository extends JpaRepository<IncidentJpaEntity, UUID> {

	List<IncidentJpaEntity> findTop50ByOrderByCreatedAtDesc();

	List<IncidentJpaEntity> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);
}
