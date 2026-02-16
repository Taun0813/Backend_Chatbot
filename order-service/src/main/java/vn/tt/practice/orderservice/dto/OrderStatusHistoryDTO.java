package vn.tt.practice.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.orderservice.enums.OrderStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDTO {
    private Long id;
    private Long orderId;
    private OrderStatus status;
    private String notes;
    private Instant createdAt;
    private String createdBy;
}
