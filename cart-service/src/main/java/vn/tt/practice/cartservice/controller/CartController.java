package vn.tt.practice.cartservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.cartservice.dto.AddToCartRequest;
import vn.tt.practice.cartservice.dto.CartDTO;
import vn.tt.practice.cartservice.dto.UpdateCartItemRequest;
import vn.tt.practice.cartservice.service.CartService;

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

    private boolean hasAdminRole(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Roles");
        if (roles == null) return false;
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartDTO> getCart(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(cartService.getCartDTO(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user's cart (ADMIN)")
    public ResponseEntity<?> getCartByUserId(@PathVariable Long userId, HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cartService.getCartDTO(userId));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<Map<String, String>> addItem(
            @RequestBody @Valid AddToCartRequest req,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        cartService.addItem(userId, req.getProductId(), req.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Added to cart"));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<Map<String, String>> updateItem(
            @PathVariable Long itemId,
            @RequestBody @Valid UpdateCartItemRequest req,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        cartService.updateItemQuantity(userId, itemId, req.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Map<String, String>> removeItem(
            @PathVariable Long itemId,
            HttpServletRequest request
    ) {
        Long userId = getUserId(request);
        cartService.removeItemByItemId(userId, itemId);
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

