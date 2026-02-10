package vn.tt.practice.productservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    private Long categoryId;
    private String brand;
    private String model;
    private Boolean isActive;
    private List<ProductCreateRequest.ProductImageRequest> images;
    private List<ProductCreateRequest.ProductSpecRequest> specs;
}
