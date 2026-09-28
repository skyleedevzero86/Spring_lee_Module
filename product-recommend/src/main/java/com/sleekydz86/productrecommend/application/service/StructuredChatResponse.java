package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.domain.product.RecommendationResponse;

public record StructuredChatResponse(
	String message,
	RecommendationResponse recommendation
) {
}
