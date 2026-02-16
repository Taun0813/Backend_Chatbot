package vn.tt.practice.cartservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.cartservice.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByIdAndCart_UserId(Long itemId, Long userId);

    List<CartItem> findByProductId(Long productId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);

    void deleteByProductId(Long productId);
}

