package com.sleekydz86.strategy.service;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import com.sleekydz86.strategy.domain.dto.ProductSearchResponse;
import com.sleekydz86.strategy.domain.repository.ProductRepository;
import com.sleekydz86.strategy.domain.service.ProductService;
import com.sleekydz86.strategy.global.strategy.CrudStrategy;
import com.sleekydz86.strategy.global.strategy.StrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private CrudStrategy crudStrategy;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setStock(50);
    }

    @Test
    @DisplayName("CRUD 작업 실행 성공")
    void testExecuteCrudOperation() {
        // given
        when(strategyFactory.getStrategy("INSERT")).thenReturn(crudStrategy);
        when(crudStrategy.execute(any(Product.class))).thenReturn(1);
        
        // when
        int result = productService.executeCrudOperation("INSERT", product);
        
        // then
        assertEquals(1, result);
        verify(strategyFactory, times(1)).getStrategy("INSERT");
        verify(crudStrategy, times(1)).execute(product);
    }

    @Test
    @DisplayName("ID로 상품 조회 성공")
    void testFindById() {
        // given
        when(productRepository.findById(anyLong())).thenReturn(product);
        
        // when
        Product result = productService.findById(1L);
        
        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID로 상품 조회 실패 - 상품을 찾을 수 없음")
    void testFindByIdNotFound() {
        // given
        when(productRepository.findById(anyLong())).thenReturn(null);
        
        // when
        Product result = productService.findById(1L);
        
        // then
        assertNull(result);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("뷰를 통한 상품 검색 성공")
    void testSearchByView() {
        // given
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Test");
        request.setPage(0);
        request.setSize(10);
        
        List<Product> products = new ArrayList<>();
        products.add(product);
        
        when(productRepository.searchByView(any(ProductSearchRequest.class))).thenReturn(products);
        when(productRepository.countByView(any(ProductSearchRequest.class))).thenReturn(1L);
        
        // when
        ProductSearchResponse response = productService.searchByView(request);
        
        // then
        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        assertEquals(1L, response.getTotalCount());
        verify(productRepository, times(1)).searchByView(request);
        verify(productRepository, times(1)).countByView(request);
    }

    @Test
    @DisplayName("프로시저 실행 성공 - INSERT")
    void testExecuteProcedure() {
        // given
        doAnswer(invocation -> {
            Map<String, Object> map = invocation.getArgument(0);
            map.put("resultId", 1L);
            map.put("affectedRows", 1);
            return null;
        }).when(productRepository).executeProcedure(any(Map.class));
        
        // when
        int result = productService.executeProcedure("INSERT", product);
        
        // then
        assertEquals(1, result);
        assertEquals(1L, product.getId());
        verify(productRepository, times(1)).executeProcedure(any(Map.class));
    }

    @Test
    @DisplayName("프로시저 실행 성공 - UPDATE")
    void testExecuteProcedureWithUpdate() {
        // given
        doAnswer(invocation -> {
            Map<String, Object> map = invocation.getArgument(0);
            map.put("resultId", 1L);
            map.put("affectedRows", 1);
            return null;
        }).when(productRepository).executeProcedure(any(Map.class));
        
        // when
        int result = productService.executeProcedure("UPDATE", product);
        
        // then
        assertEquals(1, result);
        verify(productRepository, times(1)).executeProcedure(any(Map.class));
    }
}




