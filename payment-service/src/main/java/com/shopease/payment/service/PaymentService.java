package com.shopease.payment.service;

import com.shopease.payment.dto.PaymentRequest;
import com.shopease.payment.dto.PaymentResponse;
import com.shopease.payment.entity.Payment;
import com.shopease.payment.kafka.PaymentEventProducer;
import com.shopease.payment.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setStatus(Payment.PaymentStatus.PENDING);

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().multiply(new java.math.BigDecimal("100")).longValue())
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .putMetadata("orderId", request.getOrderId().toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStripeClientSecret(intent.getClientSecret());
        } catch (StripeException e) {
            log.warn("Stripe unavailable, creating payment record without PaymentIntent: {}", e.getMessage());
        }

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        // Verify and process Stripe webhook
        try {
            com.stripe.net.Webhook.constructEvent(payload, sigHeader, webhookSecret);
            // Parse payload to get event type and update payment status
            log.info("Processed Stripe webhook event");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid webhook signature");
        }
    }

    public PaymentResponse getPaymentByOrderId(java.util.UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order: " + orderId));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public void markPaymentSuccess(String paymentIntentId) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            paymentEventProducer.publishPaymentSuccess(payment);
        });
    }
}
