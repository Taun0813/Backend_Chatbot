package vn.tt.practice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CartItemResponse {
    private UUID productId;
    private int quantity;
}
