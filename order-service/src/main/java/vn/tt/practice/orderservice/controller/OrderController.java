package vn.tt.practice.orderservice.controller;

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
import vn.tt.practice.orderservice.dto.CreateOrderRequest;
import vn.tt.practice.orderservice.dto.OrderDTO;
import vn.tt.practice.orderservice.dto.OrderStatusHistoryDTO;
import vn.tt.practice.orderservice.dto.OrderStatusUpdateRequest;
import vn.tt.practice.orderservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "APIs for order management")
public class OrderController {

    private final OrderService orderService;

    private Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
    }

    private boolean hasAdminRole(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Roles");
        if (roles == null) return false;
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN");
    }

    @PostMapping
    @Operation(summary = "Create new order")
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/user/me")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's orders (ADMIN)")
    public ResponseEntity<?> getUserOrdersByUserId(@PathVariable Long userId, HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping
    @Operation(summary = "Get all orders (ADMIN, paginated)")
    public ResponseEntity<?> getAllOrders(Pageable pageable, HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Page<OrderDTO> page = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status (ADMIN)")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (!hasAdminRole(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @GetMapping("/{id}/status-history")
    @Operation(summary = "Get order status history")
    public ResponseEntity<List<OrderStatusHistoryDTO>> getOrderStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderStatusHistory(id));
    }
}
