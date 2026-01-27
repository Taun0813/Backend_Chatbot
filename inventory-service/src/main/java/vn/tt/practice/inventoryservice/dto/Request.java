package vn.tt.practice.inventoryservice.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class Request {
    private UUID productId;
    private UUID orderId;
    private int quantity;
}

