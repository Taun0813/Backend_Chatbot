package vn.tt.practice.warrantyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.warrantyservice.enums.WarrantyStatus;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyDTO {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long userId;
    private String warrantyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer warrantyPeriodMonths;
    private WarrantyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
