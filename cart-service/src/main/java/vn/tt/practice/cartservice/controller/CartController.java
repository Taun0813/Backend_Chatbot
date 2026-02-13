package vn.tt.practice.cartservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.cartservice.dto.AddCartItemRequest;
import vn.tt.practice.cartservice.dto.CartResponse;
import vn.tt.practice.cartservice.service.CartService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Controller", description = "APIs for shopping cart management")
public class CartController {

    private final CartService cartService;

    private Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartResponse> getCart(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(new CartResponse(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<Map<String, String>> addItem(
            @RequestBody @Valid AddCartItemRequest req,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        // TODO: Fetch product details from Product Service via Feign
        // For now, using placeholder values
        cartService.addItem(
                userId,
                req.getProductId(),
                "Product " + req.getProductId(), // TODO: Fetch from Product Service
                BigDecimal.valueOf(100000), // TODO: Fetch from Product Service
                req.getQuantity()
        );
        return ResponseEntity.ok(Map.of("message", "Added to cart"));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<Map<String, String>> updateItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            HttpServletRequest request
    ) {
        // TODO: Implement update item quantity
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Map<String, String>> removeItem(
            @PathVariable Long productId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok(Map.of("message", "Removed"));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart")
    public ResponseEntity<Map<String, String>> clearCart(HttpServletRequest request) {
        Long userId = getUserId(request);
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}

