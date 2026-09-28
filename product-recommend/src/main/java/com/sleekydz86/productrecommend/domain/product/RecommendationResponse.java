package com.sleekydz86.productrecommend.domain.product;

import java.util.List;

public record RecommendationResponse(
	List<Long> productIds,
	String explanation,
	List<EvidenceItem> evidences
) {
	public RecommendationResponse {
		productIds = productIds == null ? List.of() : List.copyOf(productIds);
		evidences = evidences == null ? List.of() : List.copyOf(evidences);
	}

	public record EvidenceItem(Long productId, double score, List<String> reasons) {
		public EvidenceItem {
			reasons = reasons == null ? List.of() : List.copyOf(reasons);
		}
	}
}
