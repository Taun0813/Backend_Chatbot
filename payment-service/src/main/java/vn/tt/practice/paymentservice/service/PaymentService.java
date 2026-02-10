package vn.tt.practice.paymentservice.service;

import org.springframework.data.domain.Page;
import vn.tt.practice.paymentservice.dto.PaymentCallbackDTO;
import vn.tt.practice.paymentservice.dto.PaymentDTO;
import vn.tt.practice.paymentservice.dto.PaymentRequest;
import vn.tt.practice.paymentservice.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentDTO getPaymentById(Long id);
    PaymentDTO getPaymentByOrderId(Long orderId);
    PaymentResponse handleCallback(PaymentCallbackDTO callback);
    PaymentResponse refundPayment(Long paymentId);
    Page<PaymentDTO> getAllPayments(int page, int size);
}
