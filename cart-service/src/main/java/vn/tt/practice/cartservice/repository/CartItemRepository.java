package vn.tt.practice.cartservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.cartservice.model.CartItem;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);
}

