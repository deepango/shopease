package com.shopease.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.created", groupId = "notification-service-group")
    public void handleOrderCreated(String message) {
        log.info("Received order.created event: {}", message);
        try {
            Map<?, ?> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            String userId = (String) event.get("userId");
            String customerEmail = (String) event.get("customerEmail");

            log.info("Sending order confirmation notification for orderId={}, userId={}", orderId, userId);
            if (customerEmail != null && !customerEmail.isBlank()) {
                notificationService.sendOrderConfirmation(customerEmail, orderId);
            } else {
                log.warn("No customerEmail in order.created event for orderId={}, skipping email", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to process order.created event: {}", message, e);
        }
    }
}