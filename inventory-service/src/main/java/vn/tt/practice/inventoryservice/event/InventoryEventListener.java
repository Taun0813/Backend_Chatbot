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
        log.info("Received OrderCreatedEvent: orderId={}, items={}", event.getOrderId(), event.getItems());
        
        try {
            Long orderId = event.getOrderId();
            
            // Process each order item
            for (OrderItemEvent item : event.getItems()) {
                Long productId = item.getProductId();
                Integer quantity = item.getQuantity();
                
                try {
                    // Check and reserve stock
                    inventoryService.reserve(
                            vn.tt.practice.inventoryservice.dto.Request.builder()
                                    .productId(productId)
                                    .orderId(orderId)
                                    .quantity(quantity)
                                    .build()
                    );
                    
                    // Publish success event
                    eventPublisher.publishInventoryReserved(orderId, productId, quantity);
                    log.info("Successfully reserved inventory: orderId={}, productId={}, quantity={}", 
                            orderId, productId, quantity);
                    
                } catch (Exception e) {
                    log.error("Failed to reserve inventory: orderId={}, productId={}, quantity={}", 
                            orderId, productId, quantity, e);
                    
                    // Publish failure event
                    eventPublisher.publishInventoryReservationFailed(
                            orderId, 
                            "Insufficient stock for product " + productId + ": " + e.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
            throw e; // Let RabbitMQ retry
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

    private void confirmReservations(Long orderId) {
        var reservations = reservationRepository.findByOrderId(orderId);
        
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == InventoryReservation.Status.RESERVED) {
                // Confirm reservation - stock already reduced from available when reserved
                Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + reservation.getProductId()));
                
                // Move from reserved to confirmed (stock already reduced from available)
                inventory.setReservedStock(inventory.getReservedStock() - reservation.getQuantity());
                inventory.setUpdatedAt(Instant.now());
                inventoryRepository.save(inventory);
                
                reservation.setStatus(InventoryReservation.Status.CONFIRMED);
                reservationRepository.save(reservation);
                
                // Publish stock updated event
                eventPublisher.publishStockUpdated(reservation.getProductId(), inventory.getAvailableStock());
                
                log.info("Confirmed reservation: reservationId={}, productId={}, quantity={}", 
                        reservation.getId(), reservation.getProductId(), reservation.getQuantity());
            }
        }
    }

    private void releaseReservations(Long orderId) {
        var reservations = reservationRepository.findByOrderId(orderId);
        
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == InventoryReservation.Status.RESERVED) {
                // Release reserved stock back to available
                Inventory inventory = inventoryRepository.findByProductId(reservation.getProductId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + reservation.getProductId()));
                
                inventory.setAvailableStock(inventory.getAvailableStock() + reservation.getQuantity());
                inventory.setReservedStock(inventory.getReservedStock() - reservation.getQuantity());
                inventory.setUpdatedAt(Instant.now());
                inventoryRepository.save(inventory);
                
                reservation.setStatus(InventoryReservation.Status.RELEASED);
                reservationRepository.save(reservation);
                
                // Publish stock updated event
                eventPublisher.publishStockUpdated(reservation.getProductId(), inventory.getAvailableStock());
                
                log.info("Released reservation: reservationId={}, productId={}, quantity={}", 
                        reservation.getId(), reservation.getProductId(), reservation.getQuantity());
            }
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
