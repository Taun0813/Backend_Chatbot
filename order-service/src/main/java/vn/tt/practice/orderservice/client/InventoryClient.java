package vn.tt.practice.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/inventory/reserve")
    void reserve(@RequestBody ReserveRequest request);

    record ReserveRequest(
            Long productId,
            Long orderId,
            Integer quantity
    ) {}
}
