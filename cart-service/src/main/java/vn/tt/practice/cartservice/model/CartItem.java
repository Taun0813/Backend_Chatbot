package vn.tt.practice.cartservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "product_id"})
        }
)
public class CartItem {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    CartItem(Cart cart, UUID productId, int quantity) {
        this.id = UUID.randomUUID();
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
    }

    void increase(int amount) {
        this.quantity += amount;
    }
}
