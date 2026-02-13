package vn.tt.practice.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.model.InventoryReservation;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.repository.InventoryReservationRepository;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCleanupService {

    private final InventoryReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Cleanup expired reservations every 5 minutes
     * Releases reserved quantity back to available quantity
     */
    @Scheduled(fixedRate = 300_000) // 5 minutes
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Starting cleanup of expired reservations");

        Instant now = Instant.now();

        List<InventoryReservation> expiredReservations =
                reservationRepository.findAllByStatusAndExpiresAtBefore(
                        InventoryReservation.ReservationStatus.PENDING,
                        now
                );

        if (expiredReservations.isEmpty()) {
            log.debug("No expired reservations found");
            return;
        }

        log.info("Found {} expired reservations to cleanup", expiredReservations.size());

        for (InventoryReservation reservation : expiredReservations) {
            try {
                Inventory inventory = reservation.getInventory();
                Long productId = inventory.getProductId();
                int qty = reservation.getQuantity();

                // (khuyến nghị) lock lại inventory để tránh race khi có event confirm/release song song
                inventory = inventoryRepository.lockByProductId(productId)
                        .orElseThrow(() -> new IllegalStateException("Inventory not found productId=" + productId));

                // guard chống âm reserved
                if (inventory.getReservedQuantity() < qty) {
                    log.warn("Skip cleanup due to inconsistent reservedQuantity: reservationId={}, productId={}, reserved={}, qty={}",
                            reservation.getId(), productId, inventory.getReservedQuantity(), qty);
                    // đánh dấu EXPIRED để không loop mãi (tuỳ chiến lược)
                    reservation.setStatus(InventoryReservation.ReservationStatus.EXPIRED);
                    reservationRepository.save(reservation);
                    continue;
                }

                // Release reserved -> available
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + qty);
                inventory.setReservedQuantity(inventory.getReservedQuantity() - qty);
                inventory.setUpdatedAt(Instant.now());
                inventoryRepository.save(inventory);

                // Mark reservation expired (đúng nghĩa hơn RELEASED)
                reservation.setStatus(InventoryReservation.ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);

                log.info("Expired reservation released: reservationId={}, productId={}, quantity={}",
                        reservation.getId(), productId, qty);

            } catch (Exception e) {
                log.error("Error cleaning up expired reservation: reservationId={}",
                        reservation.getId(), e);
            }
        }

        log.info("Completed cleanup of expired reservations");
    }
}

