package vn.tt.practice.productservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductSearchRequest {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDir = "desc";
}
