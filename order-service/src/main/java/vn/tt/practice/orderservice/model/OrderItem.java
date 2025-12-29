package vn.tt.practice.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@Table(name = "order_items")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private UUID productId;
    private int quantity;
    private int price;

}
