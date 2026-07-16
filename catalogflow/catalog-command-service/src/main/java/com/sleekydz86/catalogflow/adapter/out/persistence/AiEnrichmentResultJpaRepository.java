package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.AiEnrichmentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiEnrichmentResultJpaRepository extends JpaRepository<AiEnrichmentResultEntity, UUID> {

	Optional<AiEnrichmentResultEntity> findFirstByProductIdOrderByCreatedAtDesc(UUID productId);
}
