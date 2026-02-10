package vn.tt.practice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private List<CartItemDTO> items;
    private Instant createdAt;
    private Instant updatedAt;
}
