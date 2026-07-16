package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

	@Query("select e from OutboxEventEntity e where e.published = false order by e.createdAt asc")
	List<OutboxEventEntity> findUnpublishedEvents();

	@Query("update OutboxEventEntity e set e.published = true, e.publishedAt = :publishedAt where e.id in :ids")
	@org.springframework.data.jpa.repository.Modifying
	int markPublished(@Param("ids") List<UUID> ids, @Param("publishedAt") Instant publishedAt);
}
