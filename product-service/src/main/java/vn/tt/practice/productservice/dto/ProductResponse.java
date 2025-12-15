package vn.tt.practice.productservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String brand,
        BigDecimal price,
        Integer stock,
        String shortDescription,
        String description,
        String thumbnail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}


