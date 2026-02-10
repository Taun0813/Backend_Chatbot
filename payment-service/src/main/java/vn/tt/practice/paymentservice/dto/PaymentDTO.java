package vn.tt.practice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.paymentservice.enums.PaymentMethod;
import vn.tt.practice.paymentservice.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String transactionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayResponse;
    private String errorMessage;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}
