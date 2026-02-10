package vn.tt.practice.productservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String model;
    private Boolean isActive;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createdAt;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<ProductImageDTO> images;
    private List<ProductSpecDTO> specs;

    @Data
    public static class ProductImageDTO {
        private Long id;
        private String imageUrl;
        private Boolean isPrimary;
        private Integer displayOrder;
    }

    @Data
    public static class ProductSpecDTO {
        private Long id;
        private String specKey;
        private String specValue;
    }
}
