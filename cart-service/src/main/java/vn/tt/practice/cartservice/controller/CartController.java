package vn.tt.practice.cartservice.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.cartservice.dto.AddCartItemRequest;
import vn.tt.practice.cartservice.dto.CartResponse;
import vn.tt.practice.cartservice.service.CartService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // demo: lấy userId từ header
    private UUID getUserId(HttpServletRequest request) {
        return UUID.fromString(request.getHeader("X-User-Id"));
    }

    @GetMapping
    public CartResponse getCart(HttpServletRequest request) {
        UUID userId = getUserId(request);
        return new CartResponse(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public Map<String, String> addItem(
            @RequestBody @Validated AddCartItemRequest req,
            HttpServletRequest request
    ) {
        cartService.addItem(
                getUserId(request),
                req.getProductId(),
                req.getQuantity()
        );
        return Map.of("message", "Added to cart");
    }

    @DeleteMapping("/items/{productId}")
    public Map<String, String> removeItem(
            @PathVariable UUID productId,
            HttpServletRequest request
    ) {
        cartService.removeItem(getUserId(request), productId);
        return Map.of("message", "Removed");
    }
}

