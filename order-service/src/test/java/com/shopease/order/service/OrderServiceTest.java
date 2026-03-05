package com.shopease.order.service;

import com.shopease.order.dto.OrderItemRequest;
import com.shopease.order.dto.OrderRequest;
import com.shopease.order.dto.OrderResponse;
import com.shopease.order.entity.Order;
import com.shopease.order.kafka.OrderEventProducer;
import com.shopease.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private UUID userId;
    private UUID orderId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        testOrder = new Order();
        testOrder.setId(orderId);
        testOrder.setUserId(userId);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.ZERO);
    }

    @Test
    void placeOrder_ShouldCreateOrderAndPublishEvent() {
        OrderRequest request = new OrderRequest();
        request.setUserId(userId);
        request.setShippingAddress("123 Main St");

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID());
        item.setQty(2);
        request.setItems(List.of(item));

        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(orderEventProducer).publishOrderCreated(any(Order.class));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).publishOrderCreated(any(Order.class));
    }

    @Test
    void getOrderById_ShouldReturnOrderWhenFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        OrderResponse response = orderService.getOrderById(orderId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
    }

    @Test
    void getOrderById_ShouldThrowWhenNotFound() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void getOrdersByUser_ShouldReturnUserOrders() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(testOrder));

        List<OrderResponse> responses = orderService.getOrdersByUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(userId);
    }
}
