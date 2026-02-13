package vn.tt.practice.orderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.orderservice.dto.CreateOrderRequest;
import vn.tt.practice.orderservice.dto.OrderDTO;
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
}
