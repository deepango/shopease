package com.shopease.payment.controller;

import com.shopease.payment.dto.PaymentRequest;
import com.shopease.payment.dto.PaymentResponse;
import com.shopease.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment processing via Stripe")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment and create Stripe PaymentIntent")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Handle Stripe webhook events")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("Webhook processed");
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment status by order ID")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
