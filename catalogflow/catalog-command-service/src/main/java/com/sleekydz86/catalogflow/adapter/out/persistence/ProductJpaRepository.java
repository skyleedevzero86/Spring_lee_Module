package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

	@EntityGraph(attributePaths = {"images", "keywords", "tags"})
	Optional<ProductEntity> findWithDetailsById(UUID id);

	@Query("select count(p) > 0 from ProductEntity p where p.id = :id and p.deleted = false")
	boolean existsActiveById(@Param("id") UUID id);
}
