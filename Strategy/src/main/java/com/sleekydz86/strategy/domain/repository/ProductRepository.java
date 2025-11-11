package com.sleekydz86.strategy.domain.repository;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductRepository {
    
    int insert(Product product);
    
    int update(Product product);
    
    int delete(@Param("id") Long id);
    
    Product findById(@Param("id") Long id);
    
    int executeProcedure(Map<String, Object> params);
    
    List<Product> searchByView(ProductSearchRequest request);
    
    Long countByView(ProductSearchRequest request);
}

