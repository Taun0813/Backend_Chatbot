package vn.tt.practice.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.tt.practice.inventoryservice.dto.Request;
import vn.tt.practice.inventoryservice.dto.Response;
import vn.tt.practice.inventoryservice.event.InventoryEventPublisher;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.model.InventoryReservation;
import vn.tt.practice.inventoryservice.model.InventoryTransaction;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.repository.InventoryReservationRepository;
import vn.tt.practice.inventoryservice.repository.InventoryTransactionRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String TX_TYPE_IMPORT = "IMPORT";
    private static final String TX_TYPE_RESERVE = "RESERVE";
    private static final String TX_TYPE_RELEASE = "RELEASE";
    private static final String TX_TYPE_CONFIRM = "CONFIRM";

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryEventPublisher eventPublisher;

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

        transactionRepository.save(InventoryTransaction.builder()
                .inventory(inventory)
                .transactionType(TX_TYPE_RESERVE)
                .quantity(-request.getQuantity())
                .referenceId(request.getOrderId())
                .referenceType("ORDER")
                .notes("Reserve for order " + request.getOrderId())
                .build());

        return Response.builder()
                .reserved(true)
                .reservationId(savedReservation.getId())
                .expiresAt(savedReservation.getExpiresAt())
                .message("Stock reserved successfully")
                .build();
    }

    @Transactional
    public void confirmReservation(Long orderId) {
        List<InventoryReservation> reservations = inventoryReservationRepository.findAllByOrderId(orderId);
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) continue;

            Inventory inventory = reservation.getInventory();
            Long productId = inventory.getProductId();
            int qty = reservation.getQuantity();

            if (inventory.getReservedQuantity() < qty) {
                throw new IllegalStateException("Reserved inconsistent productId=" + productId);
            }

            inventory.setReservedQuantity(inventory.getReservedQuantity() - qty);
            inventory.setUpdatedAt(Instant.now());
            inventoryRepository.save(inventory);

            reservation.setStatus(InventoryReservation.ReservationStatus.CONFIRMED);
            inventoryReservationRepository.save(reservation);

            transactionRepository.save(InventoryTransaction.builder()
                    .inventory(inventory)
                    .transactionType(TX_TYPE_CONFIRM)
                    .quantity(-qty)
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .notes("Confirm reservation for order " + orderId)
                    .build());

            eventPublisher.publishStockUpdated(productId, inventory.getAvailableQuantity());
        }
    }

    @Transactional
    public void releaseReservation(Long orderId) {
        List<InventoryReservation> reservations = inventoryReservationRepository.findAllByOrderId(orderId);
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != InventoryReservation.ReservationStatus.PENDING) continue;

            Inventory inventory = reservation.getInventory();
            Long productId = inventory.getProductId();
            int qty = reservation.getQuantity();

            if (inventory.getReservedQuantity() < qty) {
                throw new IllegalStateException("Reserved inconsistent productId=" + productId);
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + qty);
            inventory.setReservedQuantity(inventory.getReservedQuantity() - qty);
            inventory.setUpdatedAt(Instant.now());
            inventoryRepository.save(inventory);

            reservation.setStatus(InventoryReservation.ReservationStatus.RELEASED);
            inventoryReservationRepository.save(reservation);

            transactionRepository.save(InventoryTransaction.builder()
                    .inventory(inventory)
                    .transactionType(TX_TYPE_RELEASE)
                    .quantity(qty)
                    .referenceId(orderId)
                    .referenceType("ORDER")
                    .notes("Release reservation for order " + orderId)
                    .build());

            eventPublisher.publishStockUpdated(productId, inventory.getAvailableQuantity());
        }
    }

    @Transactional
    public Inventory restock(Long productId, int quantity, String createdBy) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setUpdatedAt(Instant.now());
        inventory = inventoryRepository.save(inventory);

        transactionRepository.save(InventoryTransaction.builder()
                .inventory(inventory)
                .transactionType(TX_TYPE_IMPORT)
                .quantity(quantity)
                .referenceType("RESTOCK")
                .notes("Restock by admin")
                .createdBy(createdBy)
                .build());

        eventPublisher.publishStockUpdated(productId, inventory.getAvailableQuantity());
        return inventory;
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
