package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentPromptTemplate {

	public String render(AiEnrichmentPort.EnrichmentRequest request) {
		return """
				상품명: %s
				원본설명: %s
				카테고리ID: %s
				공급사: %s
				요청결과: 요약, 상세설명, 키워드, 추천카테고리, 태그, 주의문구, 검토필요여부
				""".formatted(
				safe(request.name()),
				safe(request.description()),
				request.categoryId() == null ? "" : request.categoryId(),
				safe(request.supplierName()));
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}
}
