package com.shopease.payment.service;

import com.shopease.payment.entity.Payment;
import com.shopease.payment.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public ReconciliationService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void reconcilePendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                Payment.PaymentStatus.PENDING, cutoff);

        log.info("Reconciliation job: found {} pending payments older than 24h", pendingPayments.size());

        for (Payment payment : pendingPayments) {
            try {
                if (payment.getStripePaymentIntentId() != null) {
                    PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
                    if ("succeeded".equals(intent.getStatus())) {
                        payment.setStatus(Payment.PaymentStatus.SUCCESS);
                        paymentRepository.save(payment);
                        paymentEventProducer.publishPaymentSuccess(payment);
                        log.info("Reconciled payment {} as SUCCESS", payment.getId());
                    } else if ("canceled".equals(intent.getStatus())) {
                        payment.setStatus(Payment.PaymentStatus.FAILED);
                        paymentRepository.save(payment);
                        log.info("Reconciled payment {} as FAILED", payment.getId());
                    }
                } else {
                    payment.setStatus(Payment.PaymentStatus.RECONCILED);
                    paymentRepository.save(payment);
                }
            } catch (Exception e) {
                log.error("Failed to reconcile payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }
}
