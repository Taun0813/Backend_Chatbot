package vn.tt.practice.inventoryservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.inventoryservice.config.RabbitMQConfig;
import vn.tt.practice.inventoryservice.model.Inventory;
import vn.tt.practice.inventoryservice.model.InventoryReservation;
import vn.tt.practice.inventoryservice.repository.InventoryRepository;
import vn.tt.practice.inventoryservice.repository.InventoryReservationRepository;
import vn.tt.practice.inventoryservice.service.InventoryService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        Long orderId = event.getOrderId();
        log.info("Received OrderCreatedEvent: orderId={}, items={}", orderId, event.getItems());

//        // idempotent: nếu đã có reservation (PENDING/CONFIRMED) thì bỏ qua
//        if (reservationRepository.existsByOrderIdAndStatusIn(
//                orderId,
//                java.util.List.of(
//                        InventoryReservation.ReservationStatus.PENDING,
//                        InventoryReservation.ReservationStatus.CONFIRMED
//                ))) {
//            log.info("Skip OrderCreatedEvent because reservations already exist. orderId={}", orderId);
//            return;
//        }

        var items = event.getItems();

        try {
            // Reserve ALL items
            for (OrderItemEvent item : items) {
                var res = inventoryService.reserve(
                        vn.tt.practice.inventoryservice.dto.Request.builder()
                                .productId(item.getProductId())
                                .orderId(orderId)
                                .quantity(item.getQuantity())
                                .build()
                );

                // QUAN TRỌNG: reserve() của bạn không throw khi thiếu hàng -> phải check ở đây
                if (res == null || !Boolean.TRUE.equals(res.getReserved())) {
                    throw new IllegalStateException(
                            "Reservation failed for productId=" + item.getProductId()
                                    + ", qty=" + item.getQuantity()
                                    + ". " + (res != null ? res.getMessage() : "")
                    );
                }
            }

            // ALL ok -> publish success (per item hoặc 1 event tổng)
            for (OrderItemEvent item : items) {
                eventPublisher.publishInventoryReserved(orderId, item.getProductId(), item.getQuantity());
            }

            log.info("Reserved inventory for entire order successfully. orderId={}", orderId);

        } catch (Exception e) {
            log.error("Reserve failed. Compensating by releasing reservations. orderId={}", orderId, e);

            // release any reserved reservations of this order (PENDING)
            try {
                releaseReservations(orderId);
            } catch (Exception ex) {
                log.error("Compensation release failed. orderId={}", orderId, ex);
            }

            eventPublisher.publishInventoryReservationFailed(
                    orderId,
                    "Failed to reserve inventory for orderId=" + orderId + ". reason=" + e.getMessage()
            );

            // Nếu bạn muốn Rabbit retry thì giữ throw (nhưng nhớ idempotent + tránh spam event fail).
            throw e;
        }
    }



    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent: orderId={}, transactionId={}", 
                event.getOrderId(), event.getTransactionId());
        
        try {
            Long orderId = event.getOrderId();
            confirmReservations(orderId);
        } catch (Exception e) {
            log.error("Error processing PaymentCompletedEvent", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_QUEUE)
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());
        
        try {
            Long orderId = event.getOrderId();
            releaseReservations(orderId);
        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent", e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: orderId={}", event.getOrderId());
        
        try {
            Long orderId = event.getOrderId();
            releaseReservations(orderId);
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent", e);
            throw e;
        }
    }

    @Transactional
    public void confirmReservations(Long orderId) {
        var reservations = reservationRepository.findAllByOrderId(orderId);

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
            reservationRepository.save(reservation);

            eventPublisher.publishStockUpdated(productId, inventory.getAvailableQuantity());
        }
    }

    @Transactional
    public void releaseReservations(Long orderId) {
        var reservations = reservationRepository.findAllByOrderId(orderId);

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
            reservationRepository.save(reservation);

            eventPublisher.publishStockUpdated(productId, inventory.getAvailableQuantity());
        }
    }

    // Event DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class OrderCreatedEvent {
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private java.math.BigDecimal totalAmount;
        private java.util.List<OrderItemEvent> items;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productName;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PaymentCompletedEvent {
        private Long orderId;
        private String transactionId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PaymentFailedEvent {
        private Long orderId;
        private String reason;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class OrderCancelledEvent {
        private Long orderId;
    }
}
