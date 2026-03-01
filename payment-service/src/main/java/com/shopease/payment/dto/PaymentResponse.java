package com.shopease.payment.dto;

import com.shopease.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private String stripePaymentIntentId;
    private String stripeClientSecret;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.id = payment.getId();
        r.orderId = payment.getOrderId();
        r.stripePaymentIntentId = payment.getStripePaymentIntentId();
        r.stripeClientSecret = payment.getStripeClientSecret();
        r.status = payment.getStatus().name();
        r.amount = payment.getAmount();
        r.currency = payment.getCurrency();
        r.createdAt = payment.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public String getStripeClientSecret() { return stripeClientSecret; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
