package vn.tt.practice.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    private String brand;
    private String model;
    private Boolean isActive = true;
    private List<ProductImageRequest> images;
    private List<ProductSpecRequest> specs;

    @Data
    public static class ProductImageRequest {
        @NotBlank(message = "Image URL is required")
        private String imageUrl;
        private Boolean isPrimary = false;
        private Integer displayOrder = 0;
    }

    @Data
    public static class ProductSpecRequest {
        @NotBlank(message = "Spec key is required")
        private String specKey;
        @NotBlank(message = "Spec value is required")
        private String specValue;
    }
}
