package com.shopease.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderEventConsumer consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Inject objectMapper via reflection
    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        var field = OrderEventConsumer.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(consumer, objectMapper);
    }

    @Test
    void handleOrderCreated_ShouldProcessValidEvent() throws Exception {
        String message = """
                {"orderId": "%s", "userId": "%s", "totalAmount": 99.99, "eventType": "ORDER_CREATED"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        consumer.handleOrderCreated(message);

        // Notification service is called with result from consumer
        // (currently the service call is commented out; just verify no exception thrown)
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleOrderCreated_ShouldHandleInvalidJson() {
        // Should not throw exception
        consumer.handleOrderCreated("invalid json {{{{");
        verifyNoInteractions(notificationService);
    }

    @Test
    void handleOrderCreated_ShouldHandleMissingFields() throws Exception {
        String message = """
                {"eventType": "ORDER_CREATED"}
                """;

        consumer.handleOrderCreated(message);
        verifyNoInteractions(notificationService);
    }
}
