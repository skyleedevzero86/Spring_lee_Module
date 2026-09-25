package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.global.persistence.ModelPriceJpaEntity;
import com.sleekydz86.searchai.global.persistence.ModelPriceJpaRepository;
import com.sleekydz86.searchai.router.application.service.ModelCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public final class ModelPriceService {

	private static final Logger log = LoggerFactory.getLogger(ModelPriceService.class);

	private final ModelPriceJpaRepository repository;
	private final ModelCatalogService modelCatalogService;

	public ModelPriceService(ModelPriceJpaRepository repository, ModelCatalogService modelCatalogService) {
		this.repository = repository;
		this.modelCatalogService = modelCatalogService;
	}

	public double estimate(String modelId, long promptTokens, long completionTokens, Instant at) {
		if (modelId == null || modelId.isBlank()) {
			log.warn("비용 계산 생략: modelId가 비어 있어 카탈로그 기본값을 사용합니다");
			return modelCatalogService.estimateCost("gpt-5.6-terra", promptTokens, completionTokens);
		}
		Instant when = at == null ? Instant.now() : at;
		long safePrompt = Math.max(0, promptTokens);
		long safeCompletion = Math.max(0, completionTokens);
		Optional<ModelPriceJpaEntity> priced = repository.findEffective(modelId, when);
		if (priced.isPresent()) {
			ModelPriceJpaEntity p = priced.get();
			return (safePrompt / 1000.0) * p.getInputPrice()
				+ (safeCompletion / 1000.0) * p.getOutputPrice();
		}
		return modelCatalogService.estimateCost(modelId, safePrompt, safeCompletion);
	}

	@Transactional(readOnly = true)
	public List<ModelPriceJpaEntity> listAll() {
		return repository.findAll();
	}

	@Transactional
	public ModelPriceJpaEntity upsert(String model, double inputPrice, double outputPrice, Instant effectiveFrom, Instant effectiveTo) {
		if (model == null || model.isBlank()) {
			throw new IllegalArgumentException("모델명이 비어 있습니다");
		}
		if (inputPrice < 0 || outputPrice < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
		}
		Instant from = effectiveFrom == null ? Instant.now() : effectiveFrom;
		if (effectiveTo != null && !effectiveTo.isAfter(from)) {
			throw new IllegalArgumentException("effectiveTo는 effectiveFrom 이후여야 합니다");
		}
		ModelPriceJpaEntity entity = new ModelPriceJpaEntity();
		entity.setModel(model.trim());
		entity.setInputPrice(inputPrice);
		entity.setOutputPrice(outputPrice);
		entity.setEffectiveFrom(from);
		entity.setEffectiveTo(effectiveTo);
		ModelPriceJpaEntity saved = repository.save(entity);
		log.info("모델 가격 저장: model={} in={} out={} from={} to={}",
			saved.getModel(), saved.getInputPrice(), saved.getOutputPrice(),
			saved.getEffectiveFrom(), saved.getEffectiveTo());
		return saved;
	}
}
