package vn.tt.practice.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventories_chatbot")
@Entity
public class Inventory {
    @Id
    private UUID productId;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
