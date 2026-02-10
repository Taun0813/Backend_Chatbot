package vn.tt.practice.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    private String eventType; // COMPLETED, FAILED, REFUNDED
    private Long orderId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private String reason;
}
