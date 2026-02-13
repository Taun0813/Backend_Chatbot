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
     * Releases reserved stock back to available stock
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Starting cleanup of expired reservations");
        
        Instant now = Instant.now();
        List<InventoryReservation> expiredReservations = reservationRepository
                .findByStatusAndExpiredAtBefore(InventoryReservation.Status.RESERVED, now);
        
        if (expiredReservations.isEmpty()) {
            log.debug("No expired reservations found");
            return;
        }
        
        log.info("Found {} expired reservations to cleanup", expiredReservations.size());
        
        for (InventoryReservation reservation : expiredReservations) {
            try {
                // Release reserved stock back to available
                Inventory inventory = inventoryRepository
                        .findByProductId(reservation.getProductId())
                        .orElse(null);
                
                if (inventory != null) {
                    inventory.setAvailableStock(inventory.getAvailableStock() + reservation.getQuantity());
                    inventory.setReservedStock(inventory.getReservedStock() - reservation.getQuantity());
                    inventory.setUpdatedAt(Instant.now());
                    inventoryRepository.save(inventory);
                    
                    log.info("Released expired reservation: reservationId={}, productId={}, quantity={}", 
                            reservation.getId(), reservation.getProductId(), reservation.getQuantity());
                }
                
                // Mark reservation as released
                reservation.setStatus(InventoryReservation.Status.RELEASED);
                reservationRepository.save(reservation);
                
            } catch (Exception e) {
                log.error("Error cleaning up expired reservation: reservationId={}", 
                        reservation.getId(), e);
            }
        }
        
        log.info("Completed cleanup of expired reservations");
    }
}
