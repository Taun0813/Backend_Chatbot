package vn.tt.practice.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tt.practice.paymentservice.dto.PaymentRequest;
import vn.tt.practice.paymentservice.dto.PaymentResponse;
import vn.tt.practice.paymentservice.enums.PaymentStatus;
import vn.tt.practice.paymentservice.exception.PaymentFailedException;

import java.util.UUID;

@Slf4j
@Service
public class PaymentGatewayService {

    /**
     * Mock payment processing
     * Simulates 80% success rate for testing purposes
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for orderId: {}, amount: {}, method: {}", 
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());

        // Simulate processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 80% success rate
        boolean success = Math.random() < 0.8;

        if (success) {
            String transactionId = UUID.randomUUID().toString();
            log.info("Payment successful for orderId: {}, transactionId: {}", 
                    request.getOrderId(), transactionId);
            
            return PaymentResponse.builder()
                    .transactionId(transactionId)
                    .status(PaymentStatus.COMPLETED)
                    .message("Payment successful")
                    .build();
        } else {
            log.warn("Payment failed for orderId: {}", request.getOrderId());
            throw new PaymentFailedException("Payment gateway error: Insufficient funds or network timeout");
        }
    }
}
