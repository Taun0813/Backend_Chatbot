package vn.tt.practice.cartservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.cartservice.model.Cart;

import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
}
