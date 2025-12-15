package vn.tt.practice.productservice.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String brand,
        BigDecimal price,
        Integer stock,
        String shortDescription,
        String description,
        String thumbnail
) {}


