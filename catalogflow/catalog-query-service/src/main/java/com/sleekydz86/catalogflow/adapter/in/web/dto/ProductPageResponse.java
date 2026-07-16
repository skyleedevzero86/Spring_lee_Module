package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.util.List;

public record ProductPageResponse(
		List<ProductViewResponse> items,
		String nextCursor,
		boolean hasNext) {
}
