package com.sleekydz86.strategy.global.strategy;

import com.sleekydz86.strategy.domain.model.Product;

public interface CrudStrategy {
    int execute(Product product);
    String getOperationType();
}

