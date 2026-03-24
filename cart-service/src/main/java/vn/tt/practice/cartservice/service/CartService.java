package vn.tt.practice.cartservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.cartservice.client.ProductServiceClient;
import vn.tt.practice.cartservice.dto.CartDTO;
import vn.tt.practice.cartservice.dto.CartItemDTO;
import vn.tt.practice.cartservice.dto.ProductCartInfoDTO;
import vn.tt.practice.cartservice.exception.CartItemNotFoundException;
import vn.tt.practice.cartservice.entity.Cart;
import vn.tt.practice.cartservice.entity.CartItem;
import vn.tt.practice.cartservice.repository.CartItemRepository;
import vn.tt.practice.cartservice.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public Cart getCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCart(userId));
    }

    @Transactional
    public CartDTO addItem(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        ProductCartInfoDTO product = productServiceClient.getProductCartInfo(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product is inactive");
        }

        Cart cart = getCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setProductImageUrl(product.getImageUrl());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            cartItemRepository.save(item);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .productImageUrl(product.getImageUrl())
                    .quantity(quantity)
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();

            cartItemRepository.save(item);
            cart.getItems().add(item);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        Cart latest = cartRepository.findByUserIdWithItems(userId).orElse(cart);
        return toCartDTO(latest);
    }

    @Transactional
    public CartDTO updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findByIdAndCart_UserId(itemId, userId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));

        item.setQuantity(quantity);
        item.setSubtotal(item.getProductPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItemRepository.save(item);

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        updateCartTotals(cart);
        cartRepository.save(cart);

        return toCartDTO(cart);
    }

    @Transactional
    public void removeItemByItemId(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findByIdAndCart_UserId(itemId, userId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found: " + itemId));
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    public void removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        cart = cartRepository.findByUserIdWithItems(userId).orElseThrow();
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        updateCartTotals(cart);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeProductFromCarts(Long productId) {
        List<CartItem> items = cartItemRepository.findByProductId(productId);
        List<Long> cartIds = items.stream().map(i -> i.getCart().getId()).distinct().collect(Collectors.toList());
        cartItemRepository.deleteByProductId(productId);
        for (Long cartId : cartIds) {
            cartRepository.findById(cartId).ifPresent(cart -> {
                cart = cartRepository.findByUserIdWithItems(cart.getUserId()).orElse(cart);
                updateCartTotals(cart);
                cartRepository.save(cart);
            });
        }
    }

    @Transactional(readOnly = true)
    public CartDTO getCartDTO(Long userId) {
        Cart cart = getCart(userId);
        return toCartDTO(cart);
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

    private CartDTO toCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(i -> CartItemDTO.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .productPrice(i.getProductPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .productImageUrl(i.getProductImageUrl())
                        .build())
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .totalAmount(BigDecimal.valueOf(cart.getTotalAmount()))
                .totalItems(cart.getTotalItems())
                .items(items)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}

