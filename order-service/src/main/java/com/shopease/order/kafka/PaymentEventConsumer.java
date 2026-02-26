package com.shopease.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.order.entity.Order;
import com.shopease.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventConsumer(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.success", groupId = "order-service-group")
    public void handlePaymentSuccess(String message) {
        try {
            Map<?, ?> event = objectMapper.readValue(message, Map.class);
            String orderId = (String) event.get("orderId");
            if (orderId == null) {
                log.warn("Received payment.success event with no orderId");
                return;
            }
            orderRepository.findById(UUID.fromString(orderId)).ifPresent(order -> {
                order.setStatus(Order.OrderStatus.PAID);
                orderRepository.save(order);
                log.info("Updated order {} to PAID status", orderId);
            });
        } catch (Exception e) {
            log.error("Failed to process payment.success event: {}", message, e);
        }
    }
}
