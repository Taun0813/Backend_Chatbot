package vn.tt.practice.inventoryservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.tt.practice.inventoryservice.dto.Request;
import vn.tt.practice.inventoryservice.dto.Response;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.model.InventoryReservation;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.repository.InventoryReservationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public Response reserve(Request request) {
        Inventory inventory = inventoryRepository.lockByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + request.getProductId()));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            return Response.builder()
                    .reserved(false)
                    .message("Insufficient stock. Available: " + inventory.getAvailableQuantity() + ", Requested: " + request.getQuantity())
                    .build();
        }

        // Reserve stock
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        // Create reservation
        InventoryReservation reservation = InventoryReservation.builder()
                .inventory(inventory)
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .status(InventoryReservation.ReservationStatus.PENDING)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        InventoryReservation savedReservation = inventoryReservationRepository.save(reservation);

        return Response.builder()
                .reserved(true)
                .reservationId(savedReservation.getId())
                .expiresAt(savedReservation.getExpiresAt())
                .message("Stock reserved successfully")
                .build();
    }

    @Transactional
    public void confirmReservation(Long orderId) {
        InventoryReservation reservation = inventoryReservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Reservation not found for order: " + orderId));

        if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Reservation is not in PENDING status");
        }

        reservation.setStatus(InventoryReservation.ReservationStatus.CONFIRMED);
        inventoryReservationRepository.save(reservation);
    }

    @Transactional
    public void releaseReservation(Long orderId) {
        InventoryReservation reservation = inventoryReservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Reservation not found for order: " + orderId));

        Inventory inventory = reservation.getInventory();
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + reservation.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
        inventoryRepository.save(inventory);

        reservation.setStatus(InventoryReservation.ReservationStatus.RELEASED);
        inventoryReservationRepository.save(reservation);
    }
}
