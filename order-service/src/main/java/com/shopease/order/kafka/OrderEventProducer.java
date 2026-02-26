package com.shopease.order.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order.created";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Order order, String customerEmail) {
        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId().toString());
        event.put("userId", order.getUserId().toString());
        event.put("totalAmount", order.getTotalAmount());
        event.put("status", order.getStatus().name());
        event.put("customerEmail", customerEmail != null ? customerEmail : "");
        event.put("eventType", "ORDER_CREATED");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, order.getId().toString(), payload);
            log.info("Published order.created event for orderId={}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order event for orderId={}", order.getId(), e);
        }
    }
}
