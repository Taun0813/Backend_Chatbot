package vn.tt.practice.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.inventoryservice.dto.Request;
import vn.tt.practice.inventoryservice.dto.Response;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.service.InventoryService;

import java.util.UUID;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable UUID productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    @PostMapping("/reserve")
    public Response reserve(@RequestBody Request request) {
        return inventoryService.reserve(request);
    }
}
