package com.sleekydz86.searchai.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyAuditJpaRepository extends JpaRepository<PolicyAuditJpaEntity, Long> {

	List<PolicyAuditJpaEntity> findTop20ByOrderByChangedAtDesc();
}
