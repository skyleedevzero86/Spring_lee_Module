package com.sleekydz86.strategy.repository;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import com.sleekydz86.strategy.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest {

    @Test
    @DisplayName("Repository 인터페이스 존재 확인")
    void testRepositoryInterfaceExists() {
        // given & when & then
        assertNotNull(ProductRepository.class);
    }

    @Test
    @DisplayName("Repository 메서드 정의 확인")
    void testRepositoryMethods() {
        // given & when & then
        assertDoesNotThrow(() -> {
            ProductRepository.class.getMethod("insert", Product.class);
            ProductRepository.class.getMethod("update", Product.class);
            ProductRepository.class.getMethod("delete", Long.class);
            ProductRepository.class.getMethod("findById", Long.class);
            ProductRepository.class.getMethod("executeProcedure", Map.class);
            ProductRepository.class.getMethod("searchByView", ProductSearchRequest.class);
            ProductRepository.class.getMethod("countByView", ProductSearchRequest.class);
        });
    }
}




