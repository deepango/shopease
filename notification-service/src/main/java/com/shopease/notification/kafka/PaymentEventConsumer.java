package com.shopease.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.success", groupId = "notification-service-group")
    public void handlePaymentSuccess(String message) {
        log.info("Received payment.success event: {}", message);
        try {
            Map<?, ?> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            Object amount = event.get("amount");
            String currency = (String) event.get("currency");
            String customerEmail = (String) event.get("customerEmail");

            String amountStr = amount + " " + currency;
            log.info("Sending payment confirmation for orderId={}, amount={}", orderId, amountStr);
            if (customerEmail != null && !customerEmail.isBlank()) {
                notificationService.sendPaymentConfirmation(customerEmail, orderId, amountStr);
            } else {
                log.warn("No customerEmail in payment.success event for orderId={}, skipping email", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process payment.success event: {}", message, e);
        }
    }
}