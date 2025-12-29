package vn.tt.practice.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.inventoryservice.model.InventoryReservation;

import java.util.UUID;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
}
