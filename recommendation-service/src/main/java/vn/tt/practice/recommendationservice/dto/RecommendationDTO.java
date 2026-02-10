package vn.tt.practice.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.recommendationservice.enums.RecommendationType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private RecommendationType recommendationType;
    private BigDecimal score;
    private Integer rankPosition;
}
