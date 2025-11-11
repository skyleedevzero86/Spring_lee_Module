package com.sleekydz86.strategy.dto;

import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductSearchRequestTest {

    @Test
    @DisplayName("기본 생성자로 생성 시 기본값 확인")
    void testDefaultConstructor() {
        // given & when
        ProductSearchRequest request = new ProductSearchRequest();
        
        // then
        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
        assertEquals("id", request.getSortBy());
        assertEquals("ASC", request.getSortOrder());
    }

    @Test
    @DisplayName("Getter와 Setter 동작 확인")
    void testGettersAndSetters() {
        // given
        ProductSearchRequest request = new ProductSearchRequest();
        
        // when
        request.setName("Product");
        request.setMinPrice(100.0);
        request.setMaxPrice(500.0);
        request.setMinStock(10);
        request.setMaxStock(100);
        request.setSortBy("price");
        request.setSortOrder("DESC");
        request.setPage(1);
        request.setSize(20);
        
        // then
        assertEquals("Product", request.getName());
        assertEquals(100.0, request.getMinPrice());
        assertEquals(500.0, request.getMaxPrice());
        assertEquals(10, request.getMinStock());
        assertEquals(100, request.getMaxStock());
        assertEquals("price", request.getSortBy());
        assertEquals("DESC", request.getSortOrder());
        assertEquals(1, request.getPage());
        assertEquals(20, request.getSize());
    }

    @Test
    @DisplayName("toString 메서드 동작 확인")
    void testToString() {
        // given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Test");
        request.setMinPrice(100.0);
        
        // when
        String toString = request.toString();
        
        // then
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("100.0"));
    }
}




