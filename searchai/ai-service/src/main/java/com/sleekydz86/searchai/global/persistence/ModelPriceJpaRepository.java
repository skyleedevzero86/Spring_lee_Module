package com.sleekydz86.searchai.global.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ModelPriceJpaRepository extends JpaRepository<ModelPriceJpaEntity, Long> {

	@Query("""
		select p from ModelPriceJpaEntity p
		where lower(p.model) = lower(:model)
		  and p.effectiveFrom <= :at
		  and (p.effectiveTo is null or p.effectiveTo > :at)
		order by p.effectiveFrom desc
		""")
	List<ModelPriceJpaEntity> findEffectiveCandidates(@Param("model") String model, @Param("at") Instant at);

	default Optional<ModelPriceJpaEntity> findEffective(String model, Instant at) {
		if (model == null || model.isBlank() || at == null) {
			return Optional.empty();
		}
		List<ModelPriceJpaEntity> list = findEffectiveCandidates(model.trim(), at);
		return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
	}
}
