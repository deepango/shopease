package com.shopease.order.dto;

import com.shopease.order.entity.Order;
import com.shopease.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderResponse {
    private UUID id;
    private UUID userId;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.userId = order.getUserId();
        r.status = order.getStatus().name();
        r.totalAmount = order.getTotalAmount();
        r.shippingAddress = order.getShippingAddress();
        r.createdAt = order.getCreatedAt();
        r.items = order.getItems().stream().map(OrderItemResponse::from).collect(Collectors.toList());
        return r;
    }

    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private int qty;
        private BigDecimal unitPrice;

        public static OrderItemResponse from(OrderItem item) {
            OrderItemResponse r = new OrderItemResponse();
            r.id = item.getId();
            r.productId = item.getProductId();
            r.qty = item.getQty();
            r.unitPrice = item.getUnitPrice();
            return r;
        }

        public UUID getId() { return id; }
        public UUID getProductId() { return productId; }
        public int getQty() { return qty; }
        public BigDecimal getUnitPrice() { return unitPrice; }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public List<OrderItemResponse> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
