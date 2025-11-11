package com.sleekydz86.strategy.dto;

import com.sleekydz86.strategy.domain.dto.ProductSearchResponse;
import com.sleekydz86.strategy.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSearchResponseTest {

    @Test
    @DisplayName("기본 생성자로 생성 시 객체 생성 확인")
    void testDefaultConstructor() {
        // given & when
        ProductSearchResponse response = new ProductSearchResponse();
        
        // then
        assertNotNull(response);
    }

    @Test
    @DisplayName("파라미터 생성자로 생성 시 값 설정 확인")
    void testParameterizedConstructor() {
        // given
        List<Product> products = new ArrayList<>();
        products.add(new Product(1L, "Product 1", "Desc 1", 100.0, 10));
        products.add(new Product(2L, "Product 2", "Desc 2", 200.0, 20));
        
        // when
        ProductSearchResponse response = new ProductSearchResponse(products, 2L, 0, 10);
        
        // then
        assertEquals(2, response.getProducts().size());
        assertEquals(2L, response.getTotalCount());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    @DisplayName("총 페이지 수 계산 확인")
    void testTotalPagesCalculation() {
        // given
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            products.add(new Product((long) i, "Product " + i, "Desc", 100.0, 10));
        }
        
        // when
        ProductSearchResponse response = new ProductSearchResponse(products, 25L, 0, 10);
        
        // then
        assertEquals(3, response.getTotalPages());
    }

    @Test
    @DisplayName("Getter와 Setter 동작 확인")
    void testGettersAndSetters() {
        // given
        ProductSearchResponse response = new ProductSearchResponse();
        List<Product> products = new ArrayList<>();
        
        // when
        response.setProducts(products);
        response.setTotalCount(100L);
        response.setPage(1);
        response.setSize(20);
        response.setTotalPages(5);
        
        // then
        assertEquals(products, response.getProducts());
        assertEquals(100L, response.getTotalCount());
        assertEquals(1, response.getPage());
        assertEquals(20, response.getSize());
        assertEquals(5, response.getTotalPages());
    }
}




