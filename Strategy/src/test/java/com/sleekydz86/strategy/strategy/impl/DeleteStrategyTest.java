package com.sleekydz86.strategy.strategy.impl;

import com.sleekydz86.strategy.global.strategy.impl.DeleteStrategy;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteStrategyTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteStrategy deleteStrategy;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
    }

    @Test
    @DisplayName("삭제 전략 실행 성공")
    void testExecute() {
        // given
        when(productRepository.delete(anyLong())).thenReturn(1);
        
        // when
        int result = deleteStrategy.execute(product);
        
        // then
        assertEquals(1, result);
        verify(productRepository, times(1)).delete(1L);
    }

    @Test
    @DisplayName("삭제 전략의 작업 타입 확인")
    void testGetOperationType() {
        // given & when & then
        assertEquals("DELETE", deleteStrategy.getOperationType());
    }

    @Test
    @DisplayName("삭제 전략 실행 실패 - 영향받은 행이 0개")
    void testExecuteWithZeroResult() {
        // given
        when(productRepository.delete(anyLong())).thenReturn(0);
        
        // when
        int result = deleteStrategy.execute(product);
        
        // then
        assertEquals(0, result);
        verify(productRepository, times(1)).delete(1L);
    }
}




