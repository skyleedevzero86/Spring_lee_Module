package com.sleekydz86.catalogflow.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
		String name,
		String description,
		BigDecimal priceAmount,
		String priceCurrency,
		UUID categoryId,
		UUID supplierId) {
}
