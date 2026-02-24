package com.shopease.product.dto;

import com.shopease.product.entity.Product;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponse implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;
    private int stockQty;
    private UUID categoryId;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        ProductResponse r = new ProductResponse();
        r.id = product.getId();
        r.title = product.getTitle();
        r.description = product.getDescription();
        r.price = product.getPrice();
        r.stockQty = product.getStockQty();
        r.categoryId = product.getCategoryId();
        r.imageUrl = product.getImageUrl();
        r.active = product.isActive();
        r.createdAt = product.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQty() { return stockQty; }
    public UUID getCategoryId() { return categoryId; }
    public String getImageUrl() { return imageUrl; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
