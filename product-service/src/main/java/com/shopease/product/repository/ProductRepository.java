package com.shopease.product.repository;

import com.shopease.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(String keyword, Pageable pageable);
}
