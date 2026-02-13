package vn.tt.practice.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tt.practice.inventoryservice.model.InventoryReservation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    
    Optional<InventoryReservation> findByOrderId(Long orderId);

    List<InventoryReservation> findAllByOrderId(Long orderId);

    List<InventoryReservation> findAllByStatusAndExpiresAtBefore(
            InventoryReservation.ReservationStatus status,
            Instant now
    );
}
