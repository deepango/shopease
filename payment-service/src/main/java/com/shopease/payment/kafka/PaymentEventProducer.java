package com.shopease.payment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.payment.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment.success";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentSuccess(Payment payment) {
        Map<String, Object> event = new HashMap<>();
        event.put("paymentId", payment.getId().toString());
        event.put("orderId", payment.getOrderId().toString());
        event.put("amount", payment.getAmount());
        event.put("currency", payment.getCurrency());
        event.put("eventType", "PAYMENT_SUCCESS");

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, payment.getOrderId().toString(), payload);
            log.info("Published payment.success event for orderId={}", payment.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payment event", e);
        }
    }
}
