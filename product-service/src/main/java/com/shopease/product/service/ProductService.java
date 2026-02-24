package com.shopease.product.service;

import com.shopease.product.dto.ProductRequest;
import com.shopease.product.dto.ProductResponse;
import com.shopease.product.entity.Product;
import com.shopease.product.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable).map(ProductResponse::from);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQty(request.getStockQty());
        product.setCategoryId(request.getCategoryId());
        product.setImageUrl(request.getImageUrl());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQty(request.getStockQty());
        product.setCategoryId(request.getCategoryId());
        product.setImageUrl(request.getImageUrl());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    public boolean checkAndReduceStock(UUID id, int qty) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        if (product.getStockQty() < qty) {
            return false;
        }
        product.setStockQty(product.getStockQty() - qty);
        productRepository.save(product);
        return true;
    }
}
