package com.sleekydz86.catalogflow.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record ChangeProductPriceCommand(
		UUID productId,
		long expectedVersion,
		BigDecimal priceAmount,
		String priceCurrency) {
}
