package vn.tt.practice.warrantyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyClaimRequest {
    @NotNull(message = "Warranty ID is required")
    private Long warrantyId;

    @NotBlank(message = "Description is required")
    private String description;
}
