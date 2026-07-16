package com.sleekydz86.catalogflow.application.service;

import com.sleekydz86.catalogflow.application.port.out.ProductRepository;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class ProductQuerySupport {

	private final ProductRepository productRepository;

	public ProductQuerySupport(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public Product findProductOrThrow(ProductId productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
	}
}
