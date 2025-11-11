package com.sleekydz86.strategy.global.strategy.impl;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.global.strategy.CrudStrategy;
import com.sleekydz86.strategy.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class UpdateStrategy implements CrudStrategy {
    
    private final ProductRepository productRepository;
    
    public UpdateStrategy(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public int execute(Product product) {
        return productRepository.update(product);
    }
    
    @Override
    public String getOperationType() {
        return "UPDATE";
    }
}

