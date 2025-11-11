package com.sleekydz86.strategy.strategy.impl;

import com.sleekydz86.strategy.global.strategy.impl.UpdateStrategy;
import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateStrategy updateStrategy;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Updated Product");
        product.setDescription("Updated Description");
        product.setPrice(150.0);
        product.setStock(30);
    }

    @Test
    @DisplayName("수정 전략 실행 성공")
    void testExecute() {
        // given
        when(productRepository.update(any(Product.class))).thenReturn(1);
        
        // when
        int result = updateStrategy.execute(product);
        
        // then
        assertEquals(1, result);
        verify(productRepository, times(1)).update(product);
    }

    @Test
    @DisplayName("수정 전략의 작업 타입 확인")
    void testGetOperationType() {
        // given & when & then
        assertEquals("UPDATE", updateStrategy.getOperationType());
    }

    @Test
    @DisplayName("수정 전략 실행 실패 - 영향받은 행이 0개")
    void testExecuteWithZeroResult() {
        // given
        when(productRepository.update(any(Product.class))).thenReturn(0);
        
        // when
        int result = updateStrategy.execute(product);
        
        // then
        assertEquals(0, result);
        verify(productRepository, times(1)).update(product);
    }
}




