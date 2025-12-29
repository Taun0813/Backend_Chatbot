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
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public Response reserve(Request request) {

        Inventory inventory = inventoryRepository.lockByProductId(request.getProductId())
                                                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        //
        if (inventory.getAvailableStock() < request.getQuantity()) {
            throw new RuntimeException("Not enough inventory");
        }

        inventory.setAvailableStock(inventory.getAvailableStock() - request.getQuantity());

        inventory.setReservedStock(inventory.getReservedStock() + request.getQuantity());

        inventory.setUpdatedAt(Instant.now());

        inventoryRepository.save(inventory);

        InventoryReservation reservation = InventoryReservation.builder()
                .id(UUID.randomUUID())
                .productId(request.getProductId())
                .orderId(request.getOrderId())
                .quantity(request.getQuantity())
                .status(InventoryReservation.Status.RESERVED)
                .expiredAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();

        inventoryReservationRepository.save(reservation);

        return  Response.builder()
                .reserved(true)
                .reservationId(reservation.getId())
                .expiredAt(reservation.getExpiredAt())
                .build();


    }
}
