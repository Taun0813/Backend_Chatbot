package vn.tt.practice.inventoryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.inventoryservice.dto.Request;
import vn.tt.practice.inventoryservice.dto.Response;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.service.InventoryService;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Controller", description = "APIs for inventory management")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Check stock availability")
    public ResponseEntity<Inventory> getInventory(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found")));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for order")
    public ResponseEntity<Response> reserve(@RequestBody @Valid Request request) {
        return ResponseEntity.ok(inventoryService.reserve(request));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm reservation (reduce stock)")
    public ResponseEntity<String> confirmReservation(@RequestParam Long orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok("Reservation confirmed");
    }

    @PostMapping("/release")
    @Operation(summary = "Release reservation")
    public ResponseEntity<String> releaseReservation(@RequestParam Long orderId) {
        inventoryService.releaseReservation(orderId);
        return ResponseEntity.ok("Reservation released");
    }
}
