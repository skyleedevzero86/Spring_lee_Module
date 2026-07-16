package com.sleekydz86.catalogflow.application.query;

import java.util.List;

import com.sleekydz86.catalogflow.application.model.ProductView;

public record ProductPageResult(
		List<ProductView> items,
		String nextCursor,
		boolean hasNext) {
}
