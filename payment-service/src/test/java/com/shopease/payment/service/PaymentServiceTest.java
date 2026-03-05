package com.shopease.payment.service;

import com.shopease.payment.dto.PaymentRequest;
import com.shopease.payment.dto.PaymentResponse;
import com.shopease.payment.entity.Payment;
import com.shopease.payment.kafka.PaymentEventProducer;
import com.shopease.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        testPayment = new Payment();
        testPayment.setId(UUID.randomUUID());
        testPayment.setOrderId(orderId);
        testPayment.setAmount(new BigDecimal("99.99"));
        testPayment.setCurrency("USD");
        testPayment.setStatus(Payment.PaymentStatus.PENDING);

        // Inject a test stripe key so @PostConstruct doesn't fail
        try {
            var field = PaymentService.class.getDeclaredField("stripeSecretKey");
            field.setAccessible(true);
            field.set(paymentService, "sk_test_placeholder");
            var whField = PaymentService.class.getDeclaredField("webhookSecret");
            whField.setAccessible(true);
            whField.set(paymentService, "whsec_test");
        } catch (Exception e) {
            // ignore in test
        }
    }

    @Test
    void initiatePayment_ShouldCreatePendingPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("99.99"));
        request.setCurrency("USD");

        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Stripe will fail in unit test environment (no real key), that's OK
        // The service gracefully handles StripeException
        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(testPayment));

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void getPaymentByOrderId_ShouldThrowWhenNotFound() {
        when(paymentRepository.findByOrderId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void markPaymentSuccess_ShouldUpdateStatusAndPublishEvent() {
        String intentId = "pi_test_123";
        testPayment.setStripePaymentIntentId(intentId);

        when(paymentRepository.findByStripePaymentIntentId(intentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        doNothing().when(paymentEventProducer).publishPaymentSuccess(any(Payment.class));

        paymentService.markPaymentSuccess(intentId);

        assertThat(testPayment.getStatus()).isEqualTo(Payment.PaymentStatus.SUCCESS);
        verify(paymentEventProducer).publishPaymentSuccess(testPayment);
    }
}
