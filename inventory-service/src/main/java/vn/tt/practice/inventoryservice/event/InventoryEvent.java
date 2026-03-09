package vn.tt.practice.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent implements Serializable {
    private String eventType; // RESERVED, RESERVATION_FAILED, STOCK_UPDATED
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String reason;
    private Integer newQuantity;
}
