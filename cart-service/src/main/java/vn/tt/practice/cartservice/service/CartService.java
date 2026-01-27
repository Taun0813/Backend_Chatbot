package vn.tt.practice.cartservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tt.practice.cartservice.model.Cart;
import vn.tt.practice.cartservice.model.CartItem;
import vn.tt.practice.cartservice.repository.CartItemRepository;
import vn.tt.practice.cartservice.repository.CartRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Cart getCart(UUID userId) {
        return cartRepository.findById(userId)
                .orElseGet(() -> createCart(userId));
    }

    public void addItem(UUID userId, UUID productId, int quantity) {
        Cart cart = getCart(userId);

        CartItem item = cartItemRepository
                .findByCart_UserIdAndProductId(userId, productId)
                .orElse(null);

        if (item == null) {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }

        cartRepository.save(cart);
    }

    public void removeItem(UUID userId, UUID productId) {
        cartItemRepository.deleteByCart_UserIdAndProductId(userId, productId);
    }

    private Cart createCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }
}

