package com.sleekydz86.strategy.model;

import com.sleekydz86.strategy.domain.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("기본 생성자로 생성 시 초기값 확인")
    void testDefaultConstructor() {
        // given & when
        Product product = new Product();
        
        // then
        assertNotNull(product);
        assertNull(product.getId());
        assertNull(product.getName());
    }

    @Test
    @DisplayName("파라미터 생성자로 생성 시 값 설정 확인")
    void testParameterizedConstructor() {
        // given & when
        Product product = new Product(1L, "Test Product", "Description", 100.0, 50);
        
        // then
        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals("Description", product.getDescription());
        assertEquals(100.0, product.getPrice());
        assertEquals(50, product.getStock());
    }

    @Test
    @DisplayName("Getter와 Setter 동작 확인")
    void testGettersAndSetters() {
        // given
        Product product = new Product();
        LocalDateTime now = LocalDateTime.now();
        
        // when
        product.setId(1L);
        product.setName("Product Name");
        product.setDescription("Product Description");
        product.setPrice(200.0);
        product.setStock(100);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        // then
        assertEquals(1L, product.getId());
        assertEquals("Product Name", product.getName());
        assertEquals("Product Description", product.getDescription());
        assertEquals(200.0, product.getPrice());
        assertEquals(100, product.getStock());
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }
}

