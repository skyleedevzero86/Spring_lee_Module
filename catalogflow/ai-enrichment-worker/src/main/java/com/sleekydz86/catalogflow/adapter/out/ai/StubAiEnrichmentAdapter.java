package com.sleekydz86.catalogflow.adapter.out.ai;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubAiEnrichmentAdapter implements AiEnrichmentPort {

	private final AiProperties aiProperties;
	private final EnrichmentPromptTemplate enrichmentPromptTemplate;

	public StubAiEnrichmentAdapter(
			AiProperties aiProperties,
			EnrichmentPromptTemplate enrichmentPromptTemplate) {
		this.aiProperties = aiProperties;
		this.enrichmentPromptTemplate = enrichmentPromptTemplate;
	}

	@Override
	public EnrichmentResult enrich(EnrichmentRequest request) {
		enrichmentPromptTemplate.render(request);
		String name = normalize(request.name());
		String description = normalize(request.description());
		String summary = name + "의 핵심 정보를 요약한 상품입니다.";
		String generatedDescription = description.isBlank()
				? name + "에 대한 상세 설명이 자동 생성되었습니다. 관리자 검토 후 게시하세요."
				: description + " 이 상품은 AI 보조 설명을 포함합니다.";
		List<String> keywords = buildKeywords(name, description);
		List<String> tags = List.of("stub", "ai-generated", "검토필요");
		String recommendedCategory = request.categoryId() == null
				? "미분류"
				: request.categoryId().toString();
		boolean requiresReview = name.length() < 4 || description.isBlank();
		String warnings = requiresReview
				? "상품명 또는 설명이 부족하여 관리자 검토가 필요합니다"
				: "";
		double confidence = requiresReview ? 0.62d : 0.88d;
		return new EnrichmentResult(
				aiProperties.getModelName(),
				summary,
				generatedDescription,
				keywords,
				tags,
				recommendedCategory,
				warnings,
				requiresReview,
				confidence,
				aiProperties.getPromptVersion());
	}

	private List<String> buildKeywords(String name, String description) {
		Set<String> keywords = new LinkedHashSet<>();
		for (String token : (name + " " + description).split("[\\s,./|]+")) {
			String normalized = token.trim().toLowerCase(Locale.ROOT);
			if (normalized.length() < 2) {
				continue;
			}
			keywords.add(normalized);
			if (keywords.size() >= 8) {
				break;
			}
		}
		if (keywords.isEmpty()) {
			keywords.add("상품");
		}
		return new ArrayList<>(keywords);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim();
	}
}
