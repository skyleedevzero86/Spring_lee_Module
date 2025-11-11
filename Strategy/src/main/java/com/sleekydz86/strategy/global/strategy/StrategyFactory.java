package com.sleekydz86.strategy.global.strategy;

import com.sleekydz86.strategy.global.strategy.impl.DeleteStrategy;
import com.sleekydz86.strategy.global.strategy.impl.InsertStrategy;
import com.sleekydz86.strategy.global.strategy.impl.UpdateStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StrategyFactory {
    
    private final Map<String, CrudStrategy> strategies;
    
    public StrategyFactory(InsertStrategy insertStrategy,
                          UpdateStrategy updateStrategy,
                          DeleteStrategy deleteStrategy) {
        this.strategies = Map.of(
            "INSERT", insertStrategy,
            "UPDATE", updateStrategy,
            "DELETE", deleteStrategy
        );
    }
    
    public CrudStrategy getStrategy(String operationType) {
        CrudStrategy strategy = strategies.get(operationType.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported operation type: " + operationType);
        }
        return strategy;
    }
}

