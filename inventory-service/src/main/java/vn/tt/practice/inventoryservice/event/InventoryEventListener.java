package vn.tt.practice.inventoryservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.inventoryservice.config.RabbitMQConfig;
import vn.tt.practice.inventoryservice.service.InventoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final InventoryEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        Long orderId = event.getOrderId();
        var items = event.getItems();

        try {
            for (OrderItemEvent item : items) {
                var res = inventoryService.reserve(
                        vn.tt.practice.inventoryservice.dto.Request.builder()
                                .productId(item.getProductId())
                                .orderId(orderId)
                                .quantity(item.getQuantity())
                                .build()
                );

                if (res == null || !Boolean.TRUE.equals(res.getReserved())) {
                    throw new IllegalStateException(
                            "Reservation failed for productId=" + item.getProductId()
                                    + ", qty=" + item.getQuantity()
                                    + ". " + (res != null ? res.getMessage() : "")
                    );
                }
            }

            eventPublisher.publishInventoryReserved(orderId);
            log.info("Reserved inventory for entire order successfully. orderId={}", orderId);

        } catch (Exception e) {
            log.error("Reserve failed. Compensating by releasing reservations. orderId={}", orderId, e);

            try {
                inventoryService.releaseReservation(orderId);
            } catch (Exception ex) {
                log.error("Compensation release failed. orderId={}", orderId, ex);
            }

            eventPublisher.publishInventoryReservationFailed(
                    orderId,
                    "Failed to reserve inventory for orderId=" + orderId + ". reason=" + e.getMessage()
            );

            if (isBusinessReservationError(e)) {
                log.warn("Business reservation error -> ACK message (no retry). orderId={}", orderId);
                return;
            }

            throw e;
        }
    }

    private boolean isBusinessReservationError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;

        return msg.contains("Inventory not found")
                || msg.contains("Out of stock")
                || msg.contains("Reservation failed for productId=");
    }



    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent: orderId={}, transactionId={}", 
                event.getOrderId(), event.getTransactionId());
        try {
            inventoryService.confirmReservation(event.getOrderId());
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
            inventoryService.releaseReservation(event.getOrderId());
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
            inventoryService.releaseReservation(event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent", e);
            throw e;
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
