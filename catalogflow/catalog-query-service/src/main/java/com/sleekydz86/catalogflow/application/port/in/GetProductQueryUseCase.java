package com.sleekydz86.catalogflow.application.port.in;

import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;

public interface GetProductQueryUseCase {

	ProductView getById(UUID productId);
}
