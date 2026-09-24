package com.sleekydz86.searchai.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LlmUsageJpaRepository extends JpaRepository<LlmUsageJpaEntity, Long> {

	@Query("""
		select coalesce(sum(u.totalTokens), 0) from LlmUsageJpaEntity u
		where u.username = :username and u.createdAt >= :from
		""")
	long sumTokensSince(@Param("username") String username, @Param("from") Instant from);

	List<LlmUsageJpaEntity> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Instant from);

	List<LlmUsageJpaEntity> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);

	List<LlmUsageJpaEntity> findByUsernameAndCreatedAtBetweenOrderByCreatedAtAsc(
		String username,
		Instant from,
		Instant to
	);

	List<LlmUsageJpaEntity> findByUsernameAndCreatedAtGreaterThanEqual(String username, Instant from);
}
