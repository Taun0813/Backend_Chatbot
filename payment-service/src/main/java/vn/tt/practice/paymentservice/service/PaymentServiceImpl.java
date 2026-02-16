package vn.tt.practice.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.paymentservice.dto.*;
import vn.tt.practice.paymentservice.entity.Payment;
import vn.tt.practice.paymentservice.enums.PaymentStatus;
import vn.tt.practice.paymentservice.event.PaymentEventPublisher;
import vn.tt.practice.paymentservice.exception.PaymentFailedException;
import vn.tt.practice.paymentservice.exception.PaymentNotFoundException;
import vn.tt.practice.paymentservice.repository.PaymentRepository;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService gatewayService;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Check if payment already exists for this order
        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(payment -> {
                    throw new PaymentFailedException("Payment already exists for order: " + request.getOrderId());
                });

        // Create payment record
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PROCESSING)
                .build();
        payment = paymentRepository.save(Objects.requireNonNull(payment));

        try {
            // Process payment through gateway
            PaymentResponse gatewayResponse = gatewayService.processPayment(request);

            // Update payment with transaction details
            payment.setTransactionId(gatewayResponse.getTransactionId());
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse("Payment processed successfully");
            payment = paymentRepository.save(payment);

            // Publish PaymentCompletedEvent
            eventPublisher.publishPaymentCompleted(payment);

            return PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .transactionId(payment.getTransactionId())
                    .status(payment.getStatus())
                    .message("Payment completed successfully")
                    .build();

        } catch (PaymentFailedException e) {
            // Update payment with failure details
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            payment = paymentRepository.save(payment);

            // Publish PaymentFailedEvent
            eventPublisher.publishPaymentFailed(payment, e.getMessage());

            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(id, "id must not be null"))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        return toDTO(payment);
    }

    @Override
    @Transactional
    public PaymentResponse handleCallback(PaymentCallbackDTO callback) {
        Payment payment = paymentRepository.findByTransactionId(callback.getTransactionId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with transactionId: " + callback.getTransactionId()));

        if ("SUCCESS".equalsIgnoreCase(callback.getStatus())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
            payment.setGatewayResponse(callback.getMessage());
            payment = paymentRepository.save(payment);
            eventPublisher.publishPaymentCompleted(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setErrorMessage(callback.getMessage());
            payment = paymentRepository.save(payment);
            eventPublisher.publishPaymentFailed(payment, callback.getMessage());
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .message(callback.getMessage())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(Objects.requireNonNull(paymentId, "paymentId must not be null"))
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentFailedException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        eventPublisher.publishPaymentRefunded(payment);

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .message("Payment refunded successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toDTO);
    }

    private PaymentDTO toDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .gatewayResponse(payment.getGatewayResponse())
                .errorMessage(payment.getErrorMessage())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
