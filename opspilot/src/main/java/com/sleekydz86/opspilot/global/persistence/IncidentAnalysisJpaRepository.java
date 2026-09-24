package com.sleekydz86.opspilot.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentAnalysisJpaRepository extends JpaRepository<IncidentAnalysisJpaEntity, UUID> {

	Optional<IncidentAnalysisJpaEntity> findByIncidentId(UUID incidentId);

	List<IncidentAnalysisJpaEntity> findByIncidentIdIn(Collection<UUID> incidentIds);
}
