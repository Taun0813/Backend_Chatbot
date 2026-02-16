package vn.tt.practice.recommendationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.recommendationservice.enums.InteractionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long categoryId;

    @NotNull(message = "Interaction type is required")
    private InteractionType interactionType;
}

