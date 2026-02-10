package vn.tt.practice.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.paymentservice.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}
