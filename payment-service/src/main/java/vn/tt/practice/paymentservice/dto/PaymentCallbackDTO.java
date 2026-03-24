package vn.tt.practice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackDTO {
    private String transactionId;
    private String status;
    private String message;
    private String signature;
}
