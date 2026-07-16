package com.sleekydz86.catalogflow.application.batch.model;

import com.sleekydz86.catalogflow.domain.model.Product;

public record ProductImportItem(
		String productCode,
		Product product) {
}
