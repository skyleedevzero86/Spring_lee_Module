package com.sleekydz86.searchai.global.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsageAlertJpaRepository extends JpaRepository<UsageAlertJpaEntity, Long> {

	List<UsageAlertJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
