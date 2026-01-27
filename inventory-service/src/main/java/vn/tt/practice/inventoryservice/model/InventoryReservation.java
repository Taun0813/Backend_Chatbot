package vn.tt.practice.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservation {

    @Id
    private UUID id;

    private UUID productId;
    private UUID orderId;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant expiredAt;
    private Instant createdAt;

    public enum Status {
        RESERVED,
        CONFIRMED,
        RELEASED
    }
}
