package com.shopease.product.controller;

import com.shopease.product.dto.ProductRequest;
import com.shopease.product.dto.ProductResponse;
import com.shopease.product.service.ExchangeRateService;
import com.shopease.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;
    private final ExchangeRateService exchangeRateService;

    public ProductController(ProductService productService, ExchangeRateService exchangeRateService) {
        this.productService = productService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and sorting")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
        }
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID (cached)")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete product")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exchange-rates")
    @Operation(summary = "Get current USD exchange rates")
    public ResponseEntity<Map<String, Double>> getExchangeRates() {
        return ResponseEntity.ok(exchangeRateService.getUsdRates());
    }
}
