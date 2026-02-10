package vn.tt.practice.warrantyservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.warrantyservice.dto.WarrantyClaimDTO;
import vn.tt.practice.warrantyservice.dto.WarrantyClaimRequest;
import vn.tt.practice.warrantyservice.dto.WarrantyDTO;
import vn.tt.practice.warrantyservice.enums.ClaimStatus;
import vn.tt.practice.warrantyservice.service.WarrantyService;

import java.util.List;

@RestController
@RequestMapping("/warranties")
@RequiredArgsConstructor
@Tag(name = "Warranty Management", description = "APIs for managing warranties and claims")
public class WarrantyController {

    private final WarrantyService warrantyService;

    private Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            return Long.parseLong(userIdHeader);
        }
        return 1L; // Default for testing
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get warranties by order ID")
    public ResponseEntity<List<WarrantyDTO>> getWarrantiesByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(warrantyService.getWarrantiesByOrderId(orderId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warranty details")
    public ResponseEntity<WarrantyDTO> getWarrantyById(@PathVariable Long id) {
        return ResponseEntity.ok(warrantyService.getWarrantyById(id));
    }

    @GetMapping("/user/me")
    @Operation(summary = "Get my warranties")
    public ResponseEntity<List<WarrantyDTO>> getMyWarranties(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(warrantyService.getMyWarranties(userId));
    }

    @PostMapping("/claims")
    @Operation(summary = "Submit warranty claim")
    public ResponseEntity<WarrantyClaimDTO> submitClaim(
            @Valid @RequestBody WarrantyClaimRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return new ResponseEntity<>(warrantyService.submitClaim(userId, request), HttpStatus.CREATED);
    }

    @GetMapping("/claims/{id}")
    @Operation(summary = "Get claim details")
    public ResponseEntity<WarrantyClaimDTO> getClaimById(@PathVariable Long id) {
        return ResponseEntity.ok(warrantyService.getClaimById(id));
    }

    @PutMapping("/claims/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Update claim status (ADMIN)")
    public ResponseEntity<WarrantyClaimDTO> updateClaimStatus(
            @PathVariable Long id,
            @RequestParam ClaimStatus status,
            @RequestParam(required = false) String resolution) {
        return ResponseEntity.ok(warrantyService.updateClaimStatus(id, status, resolution));
    }

    @GetMapping("/claims")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get all claims (ADMIN, paginated)")
    public ResponseEntity<Page<WarrantyClaimDTO>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(warrantyService.getAllClaims(page, size));
    }
}
