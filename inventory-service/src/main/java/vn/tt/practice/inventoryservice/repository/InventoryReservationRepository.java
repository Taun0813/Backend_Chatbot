package vn.tt.practice.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tt.practice.inventoryservice.model.InventoryReservation;

import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    
    Optional<InventoryReservation> findByOrderId(Long orderId);
    
    List<InventoryReservation> findByOrderIdAndStatus(Long orderId, InventoryReservation.ReservationStatus status);
    
    @Query("SELECT r FROM InventoryReservation r WHERE r.status = 'PENDING' AND r.expiresAt < CURRENT_TIMESTAMP")
    List<InventoryReservation> findExpiredReservations();
}
