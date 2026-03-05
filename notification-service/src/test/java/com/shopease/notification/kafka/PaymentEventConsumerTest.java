package com.shopease.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentEventConsumer consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        var field = PaymentEventConsumer.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(consumer, objectMapper);
    }

    @Test
    void handlePaymentSuccess_ShouldProcessValidEvent() {
        String message = """
                {"paymentId": "%s", "orderId": "%s", "amount": 99.99, "currency": "USD", "eventType": "PAYMENT_SUCCESS"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        consumer.handlePaymentSuccess(message);
        verifyNoInteractions(notificationService);
    }

    @Test
    void handlePaymentSuccess_ShouldHandleInvalidJson() {
        consumer.handlePaymentSuccess("not valid json");
        verifyNoInteractions(notificationService);
    }

    @Test
    void handlePaymentSuccess_ShouldHandleNullAmount() {
        String message = """
                {"orderId": "%s", "currency": "USD"}
                """.formatted(UUID.randomUUID());

        consumer.handlePaymentSuccess(message);
        verifyNoInteractions(notificationService);
    }
}
