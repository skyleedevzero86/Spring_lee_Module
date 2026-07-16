package com.sleekydz86.catalogflow.application.batch.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCsvRow(
		String productCode,
		String name,
		String description,
		BigDecimal priceAmount,
		String priceCurrency,
		UUID categoryId,
		UUID supplierId,
		long lineNumber) {
}
