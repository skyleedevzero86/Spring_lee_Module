package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentResultValidator {

	public void validate(AiEnrichmentPort.EnrichmentResult result) {
		if (result == null) {
			throw new ApplicationException("AI 가공 결과가 없습니다");
		}
		if (isBlank(result.modelName())) {
			throw new ApplicationException("모델 이름은 필수입니다");
		}
		if (isBlank(result.summary())) {
			throw new ApplicationException("상품 요약은 필수입니다");
		}
		if (isBlank(result.generatedDescription())) {
			throw new ApplicationException("생성 설명은 필수입니다");
		}
		if (result.keywords() == null || result.keywords().isEmpty()) {
			throw new ApplicationException("키워드는 1개 이상이어야 합니다");
		}
		if (result.tags() == null || result.tags().isEmpty()) {
			throw new ApplicationException("태그는 1개 이상이어야 합니다");
		}
		if (result.confidence() < 0.0d || result.confidence() > 1.0d) {
			throw new ApplicationException("신뢰도 값이 올바르지 않습니다");
		}
		if (isBlank(result.promptVersion())) {
			throw new ApplicationException("프롬프트 버전은 필수입니다");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
