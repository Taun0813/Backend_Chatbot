package vn.tt.practice.cartservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tt.practice.cartservice.model.Cart;
import vn.tt.practice.cartservice.model.CartItem;
import vn.tt.practice.cartservice.repository.CartItemRepository;
import vn.tt.practice.cartservice.repository.CartRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    public void addItem(Long userId, Long productId, String productName, BigDecimal productPrice, int quantity) {
        Cart cart = getCart(userId);

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubtotal(item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .productName(productName)
                    .productPrice(productPrice)
                    .quantity(quantity)
                    .subtotal(productPrice.multiply(BigDecimal.valueOf(quantity)))
                    .build();
            cart.getItems().add(newItem);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    public void removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    public void clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private void updateCartTotals(Cart cart) {
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItems(totalItems);
        cart.setTotalAmount(totalAmount.doubleValue());
    }
}

