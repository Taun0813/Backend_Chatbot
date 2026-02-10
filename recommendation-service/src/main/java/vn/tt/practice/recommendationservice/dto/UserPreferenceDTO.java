package vn.tt.practice.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private Long categoryId;
    private Integer viewCount;
    private Integer purchaseCount;
    private Instant lastViewedAt;
    private Instant lastPurchasedAt;
    private BigDecimal preferenceScore;
    private Instant createdAt;
    private Instant updatedAt;
}
