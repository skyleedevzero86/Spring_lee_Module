package com.sleekydz86.strategy.domain.controller;

import com.sleekydz86.strategy.domain.model.Product;
import com.sleekydz86.strategy.domain.dto.ProductSearchRequest;
import com.sleekydz86.strategy.domain.dto.ProductSearchResponse;
import com.sleekydz86.strategy.domain.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<String> insert(@RequestBody Product product) {
        int result = productService.executeCrudOperation("INSERT", product);
        if (result > 0) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Product inserted successfully. ID: " + product.getId());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to insert product");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        int result = productService.executeCrudOperation("UPDATE", product);
        if (result > 0) {
            return ResponseEntity.ok("Product updated successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Product not found or update failed");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        Product product = new Product();
        product.setId(id);
        int result = productService.executeCrudOperation("DELETE", product);
        if (result > 0) {
            return ResponseEntity.ok("Product deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Product not found or delete failed");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product product = productService.findById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponse> searchByView(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setName(name);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinStock(minStock);
        request.setMaxStock(maxStock);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);

        ProductSearchResponse response = productService.searchByView(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/procedure")
    public ResponseEntity<String> executeProcedure(
            @RequestParam String operation,
            @RequestBody Product product) {
        int result = productService.executeProcedure(operation, product);
        if (result > 0) {
            return ResponseEntity.ok("Procedure executed successfully. Affected rows: " + result);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Procedure execution failed");
    }
}
