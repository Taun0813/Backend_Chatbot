package vn.tt.practice.cartservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.cartservice.model.CartItem;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCart_UserIdAndProductId(UUID userId, UUID productId);

    void deleteByCart_UserIdAndProductId(UUID userId, UUID productId);
}

