package vn.tt.practice.inventoryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.inventoryservice.dto.Request;
import vn.tt.practice.inventoryservice.dto.Response;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.model.InventoryTransaction;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.service.InventoryService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Controller", description = "APIs for inventory management")
public class InventoryController {

    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    private static Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) return Set.of();
        return Stream.of(rolesHeader.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static boolean hasAdminOrSuperAdmin(Set<String> roles) {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_SUPER_ADMIN);
    }

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

    @PostMapping("/release")
    @Operation(summary = "Release reservation")
    public ResponseEntity<String> releaseReservation(@RequestParam Long orderId) {
        inventoryService.releaseReservation(orderId);
        return ResponseEntity.ok("Reservation released");
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm reservation (reduce stock)")
    public ResponseEntity<String> confirmReservation(@RequestParam Long orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok("Reservation confirmed");
    }

    @PutMapping("/{productId}/restock")
    @Operation(summary = "Add stock (ADMIN)")
    public ResponseEntity<Inventory> restock(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader,
            @RequestHeader(value = "X-User-Id", required = false) String createdBy) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(inventoryService.restock(productId, quantity, createdBy));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history (ADMIN)")
    public ResponseEntity<Page<InventoryTransaction>> getTransactions(
            Pageable pageable,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(inventoryService.getTransactions(pageable));
    }
}
