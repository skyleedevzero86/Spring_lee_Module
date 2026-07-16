package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank @Size(max = 200) String name,
		@Size(max = 10000) String description,
		@NotNull @DecimalMin("0.0") BigDecimal priceAmount,
		@NotBlank @Size(min = 3, max = 3) String priceCurrency,
		@NotNull UUID categoryId,
		@NotNull UUID supplierId) {
}
