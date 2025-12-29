package vn.tt.practice.cartservice.dto;

import lombok.Getter;
import vn.tt.practice.cartservice.model.Cart;

import java.util.List;
import java.util.UUID;

@Getter
public class CartResponse {

    private UUID userId;
    private List<CartItemResponse> items;

    public CartResponse(Cart cart) {
        this.userId = cart.getUserId();
        this.items = cart.getItems().stream()
                .map(i -> new CartItemResponse(i.getProductId(), i.getQuantity()))
                .toList();
    }
}

