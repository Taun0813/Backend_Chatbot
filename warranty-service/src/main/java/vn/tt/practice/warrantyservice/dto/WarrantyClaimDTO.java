package vn.tt.practice.warrantyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.warrantyservice.enums.ClaimStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyClaimDTO {
    private Long id;
    private Long warrantyId;
    private Long userId;
    private String claimNumber;
    private String description;
    private ClaimStatus status;
    private String resolution;
    private Instant submittedAt;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
