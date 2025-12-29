package vn.tt.practice.orderservice.model;

import jakarta.persistence.*;
import jakarta.transaction.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders_chatbot")
@Entity
public class Order {

    @Id
    private UUID id;

    private UUID userId;
    @Enumerated(EnumType.STRING)
    private Status status;

    private long totalAmount;

    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public enum Status {
        PENDING,
        PAID,
        SHIPPED,
        CANCELLED
    }

}
