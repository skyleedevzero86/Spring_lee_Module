package com.sleekydz86.opspilot.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiUsageJpaRepository extends JpaRepository<AiUsageJpaEntity, UUID> {
}
