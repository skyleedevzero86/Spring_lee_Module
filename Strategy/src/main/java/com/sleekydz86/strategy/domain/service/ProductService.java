package com.sleekydz86.strategy.domain.service;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import com.sleekydz86.strategy.domain.dto.ProductSearchResponse;
import com.sleekydz86.strategy.domain.repository.ProductRepository;
import com.sleekydz86.strategy.global.strategy.CrudStrategy;
import com.sleekydz86.strategy.global.strategy.StrategyFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final StrategyFactory strategyFactory;
    
    public ProductService(ProductRepository productRepository, StrategyFactory strategyFactory) {
        this.productRepository = productRepository;
        this.strategyFactory = strategyFactory;
    }
    
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public int executeCrudOperation(String operationType, Product product) {
        CrudStrategy strategy = strategyFactory.getStrategy(operationType);
        return strategy.execute(product);
    }
    
    @Cacheable(value = "products", key = "#id")
    public Product findById(Long id) {
        return productRepository.findById(id);
    }
    
    @Cacheable(value = "productSearch", key = "#request.toString()")
    public ProductSearchResponse searchByView(ProductSearchRequest request) {
        List<Product> products = productRepository.searchByView(request);
        Long totalCount = productRepository.countByView(request);
        return new ProductSearchResponse(products, totalCount, request.getPage(), request.getSize());
    }
    
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public int executeProcedure(String operation, Product product) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("operation", operation);
        params.put("product", product);
        params.put("resultId", null);
        params.put("affectedRows", null);
        
        productRepository.executeProcedure(params);
        
        Long resultId = (Long) params.get("resultId");
        if (resultId != null && "INSERT".equals(operation)) {
            product.setId(resultId);
        }
        
        Integer affectedRows = (Integer) params.get("affectedRows");
        return affectedRows != null ? affectedRows : 0;
    }
}

