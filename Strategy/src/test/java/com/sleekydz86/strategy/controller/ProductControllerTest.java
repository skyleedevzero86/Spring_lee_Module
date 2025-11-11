package com.sleekydz86.strategy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.strategy.domain.controller.ProductController;
import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import com.sleekydz86.strategy.domain.dto.ProductSearchResponse;
import com.sleekydz86.strategy.domain.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("상품 등록 성공")
    void testInsert() throws Exception {
        // given
        when(productService.executeCrudOperation("INSERT", any(Product.class))).thenReturn(1);
        
        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품이 성공적으로 등록되었습니다")));
    }

    @Test
    @DisplayName("상품 등록 실패")
    void testInsertFailure() throws Exception {
        // given
        when(productService.executeCrudOperation("INSERT", any(Product.class))).thenReturn(0);
        
        // when & then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품 등록에 실패했습니다")));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void testUpdate() throws Exception {
        // given
        when(productService.executeCrudOperation("UPDATE", any(Product.class))).thenReturn(1);
        
        // when & then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품이 성공적으로 수정되었습니다")));
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품을 찾을 수 없음")
    void testUpdateNotFound() throws Exception {
        // given
        when(productService.executeCrudOperation("UPDATE", any(Product.class))).thenReturn(0);
        
        // when & then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void testDelete() throws Exception {
        // given
        when(productService.executeCrudOperation("DELETE", any(Product.class))).thenReturn(1);
        
        // when & then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("상품이 성공적으로 삭제되었습니다")));
    }

    @Test
    @DisplayName("상품 삭제 실패 - 상품을 찾을 수 없음")
    void testDeleteNotFound() throws Exception {
        // given
        when(productService.executeCrudOperation("DELETE", any(Product.class))).thenReturn(0);
        
        // when & then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void testFindById() throws Exception {
        // given
        when(productService.findById(anyLong())).thenReturn(product);
        
        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    @DisplayName("상품 ID로 조회 실패 - 상품을 찾을 수 없음")
    void testFindByIdNotFound() throws Exception {
        // given
        when(productService.findById(anyLong())).thenReturn(null);
        
        // when & then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("상품 검색 성공")
    void testSearchByView() throws Exception {
        // given
        List<Product> products = new ArrayList<>();
        products.add(product);
        ProductSearchResponse response = new ProductSearchResponse(products, 1L, 0, 10);
        when(productService.searchByView(any(ProductSearchRequest.class))).thenReturn(response);
        
        // when & then
        mockMvc.perform(get("/api/products/search")
                .param("name", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1L))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("프로시저 실행 성공")
    void testExecuteProcedure() throws Exception {
        // given
        when(productService.executeProcedure(any(String.class), any(Product.class))).thenReturn(1);
        
        // when & then
        mockMvc.perform(post("/api/products/procedure")
                .param("operation", "INSERT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("프로시저가 성공적으로 실행되었습니다")));
    }

    @Test
    @DisplayName("프로시저 실행 실패")
    void testExecuteProcedureFailure() throws Exception {
        // given
        when(productService.executeProcedure(any(String.class), any(Product.class))).thenReturn(0);
        
        // when & then
        mockMvc.perform(post("/api/products/procedure")
                .param("operation", "INSERT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("프로시저 실행에 실패했습니다")));
    }
}




