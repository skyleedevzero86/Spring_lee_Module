package com.sleekydz86.strategy.domain.dto;

import com.sleekydz86.strategy.domain.model.Product;
import java.util.List;

public class ProductSearchResponse {
    private List<Product> products;
    private Long totalCount;
    private Integer page;
    private Integer size;
    private Integer totalPages;

    public ProductSearchResponse() {
    }

    public ProductSearchResponse(List<Product> products, Long totalCount, Integer page, Integer size) {
        this.products = products;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalCount / size);
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}

