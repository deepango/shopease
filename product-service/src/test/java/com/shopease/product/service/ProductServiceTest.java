package com.shopease.product.service;

import com.shopease.product.dto.ProductRequest;
import com.shopease.product.dto.ProductResponse;
import com.shopease.product.entity.Product;
import com.shopease.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = new Product();
        testProduct.setId(productId);
        testProduct.setTitle("Test Product");
        testProduct.setDescription("A great product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQty(100);
        testProduct.setActive(true);
    }

    @Test
    void getAllProducts_ShouldReturnPagedResults() {
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        when(productRepository.findByActiveTrue(any())).thenReturn(productPage);

        Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_ShouldReturnProductFromCache() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        ProductResponse result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void getProductById_ShouldThrowWhenNotFound() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createProduct_ShouldPersistAndReturnProduct() {
        ProductRequest request = new ProductRequest();
        request.setTitle("New Product");
        request.setPrice(new BigDecimal("49.99"));
        request.setStockQty(50);

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldSoftDeleteProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteProduct(productId);

        assertThat(testProduct.isActive()).isFalse();
        verify(productRepository).save(testProduct);
    }

    @Test
    void checkAndReduceStock_ShouldReduceStockWhenAvailable() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        boolean result = productService.checkAndReduceStock(productId, 10);

        assertThat(result).isTrue();
        assertThat(testProduct.getStockQty()).isEqualTo(90);
    }

    @Test
    void checkAndReduceStock_ShouldReturnFalseWhenInsufficientStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        boolean result = productService.checkAndReduceStock(productId, 200);

        assertThat(result).isFalse();
        assertThat(testProduct.getStockQty()).isEqualTo(100);
    }
}
