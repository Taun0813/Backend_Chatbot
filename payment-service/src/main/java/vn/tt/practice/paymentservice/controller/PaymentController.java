package vn.tt.practice.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.paymentservice.dto.*;
import vn.tt.practice.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments")
public class PaymentController {

    private final PaymentService paymentService;

    private boolean hasAdminRole(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Roles");
        if (roles == null) return false;
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN");
    }

    @PostMapping("/process")
    @Operation(summary = "Process payment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(paymentService.processPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment details")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<PaymentDTO> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PostMapping("/callback")
    @Operation(summary = "Payment gateway callback")
    public ResponseEntity<PaymentResponse> handleCallback(@RequestBody PaymentCallbackDTO callback) {
        return ResponseEntity.ok(paymentService.handleCallback(callback));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment (ADMIN)")
    public ResponseEntity<?> refundPayment(@PathVariable Long id, HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @GetMapping
    @Operation(summary = "Get all payments (ADMIN, paginated)")
    public ResponseEntity<?> getAllPayments(Pageable pageable, HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }
}
