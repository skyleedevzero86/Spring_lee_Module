package com.sleekydz86.searchai.usage.adapter.in.web;

import com.sleekydz86.searchai.global.persistence.ModelPriceJpaEntity;
import com.sleekydz86.searchai.usage.application.service.ModelPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/model-prices")
public final class ModelPriceAdminController {

	private static final Logger log = LoggerFactory.getLogger(ModelPriceAdminController.class);

	private final ModelPriceService modelPriceService;

	public ModelPriceAdminController(ModelPriceService modelPriceService) {
		this.modelPriceService = modelPriceService;
	}

	@GetMapping
	public List<ModelPriceView> list() {
		return modelPriceService.listAll().stream().map(ModelPriceView::from).toList();
	}

	@PostMapping
	public ResponseEntity<?> create(@RequestBody ModelPriceRequest request) {
		try {
			ModelPriceJpaEntity saved = modelPriceService.upsert(
				request.model(),
				request.inputPrice(),
				request.outputPrice(),
				request.effectiveFrom(),
				request.effectiveTo()
			);
			return ResponseEntity.status(HttpStatus.CREATED).body(ModelPriceView.from(saved));
		} catch (IllegalArgumentException ex) {
			log.warn("모델 가격 등록 거부: {}", ex.getMessage());
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			log.error("모델 가격 등록 실패: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "모델 가격 저장 중 오류가 발생했습니다"));
		}
	}

	public record ModelPriceRequest(
		String model,
		double inputPrice,
		double outputPrice,
		Instant effectiveFrom,
		Instant effectiveTo
	) {
	}

	public record ModelPriceView(
		Long id,
		String model,
		double inputPrice,
		double outputPrice,
		Instant effectiveFrom,
		Instant effectiveTo
	) {
		static ModelPriceView from(ModelPriceJpaEntity entity) {
			return new ModelPriceView(
				entity.getId(),
				entity.getModel(),
				entity.getInputPrice(),
				entity.getOutputPrice(),
				entity.getEffectiveFrom(),
				entity.getEffectiveTo()
			);
		}
	}
}
