package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentPromptTemplate {

	public String renderSystemPrompt() {
		return """
				당신은 상품 카탈로그 정보를 보강하는 AI입니다.
				반드시 JSON 객체만 응답하세요. 마크다운이나 설명 문장은 포함하지 마세요.
				응답 필드:
				summary, generatedDescription, keywords, tags, recommendedCategory, warnings, requiresHumanReview, confidence
				keywords와 tags는 문자열 배열입니다.
				requiresHumanReview는 boolean, confidence는 0과 1 사이 숫자입니다.
				모든 텍스트는 한국어로 작성하세요.
				""";
	}

	public String renderUserPrompt(AiEnrichmentPort.EnrichmentRequest request) {
		return """
				상품명: %s
				원본설명: %s
				카테고리ID: %s
				공급사: %s
				위 정보를 바탕으로 상품 요약, 상세 설명, 검색 키워드, 추천 카테고리, 태그, 주의 문구, 검토 필요 여부를 JSON으로 생성하세요.
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
