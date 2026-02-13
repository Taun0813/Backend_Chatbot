package vn.tt.practice.cartservice.dto;

import lombok.Getter;
import vn.tt.practice.cartservice.model.Cart;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CartResponse {

    private Long userId;
    private Double totalAmount;
    private Integer totalItems;
    private List<CartItemResponse> items;

    public CartResponse(Cart cart) {
        this.userId = cart.getUserId();
        this.totalAmount = cart.getTotalAmount();
        this.totalItems = cart.getTotalItems();
        this.items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getProductName(),
                        i.getProductPrice(),
                        i.getQuantity(),
                        i.getSubtotal()
                ))
                .toList();
    }
}

