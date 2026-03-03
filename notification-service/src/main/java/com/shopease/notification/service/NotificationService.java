package com.shopease.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@shopease.com}")
    private String fromEmail;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(String toEmail, String orderId) {
        String subject = "Order Confirmation - ShopEase #" + orderId;
        String body = buildOrderConfirmationEmail(orderId);
        sendEmail(toEmail, subject, body);
    }

    public void sendPaymentConfirmation(String toEmail, String orderId, String amount) {
        String subject = "Payment Confirmed - ShopEase Order #" + orderId;
        String body = buildPaymentConfirmationEmail(orderId, amount);
        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(String orderId) {
        return """
                <html>
                <body>
                <h2>Thank you for your order!</h2>
                <p>Your order <strong>#%s</strong> has been successfully placed.</p>
                <p>We will update you once it is confirmed and shipped.</p>
                <br/>
                <p>Best regards,<br/>The ShopEase Team</p>
                </body>
                </html>
                """.formatted(orderId);
    }

    private String buildPaymentConfirmationEmail(String orderId, String amount) {
        return """
                <html>
                <body>
                <h2>Payment Confirmed!</h2>
                <p>We have received your payment of <strong>%s</strong> for order <strong>#%s</strong>.</p>
                <p>Your order is now being processed.</p>
                <br/>
                <p>Best regards,<br/>The ShopEase Team</p>
                </body>
                </html>
                """.formatted(amount, orderId);
    }
}
