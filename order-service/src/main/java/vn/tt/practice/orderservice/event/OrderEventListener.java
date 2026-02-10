package vn.tt.practice.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.orderservice.service.OrderSagaOrchestrator;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderSagaOrchestrator sagaOrchestrator;

    @RabbitListener(queues = "inventory.reserved")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for orderId: {}", event.getOrderId());
        sagaOrchestrator.handleInventoryReserved(event.getOrderId());
    }

    @RabbitListener(queues = "inventory.reservation.failed")
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.info("Received InventoryReservationFailedEvent for orderId: {}", event.getOrderId());
        sagaOrchestrator.handleInventoryReservationFailed(event.getOrderId(), event.getReason());
    }

    @RabbitListener(queues = "payment.completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for orderId: {}", event.getOrderId());
        sagaOrchestrator.handlePaymentCompleted(event.getOrderId());
    }

    @RabbitListener(queues = "payment.failed")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for orderId: {}", event.getOrderId());
        sagaOrchestrator.handlePaymentFailed(event.getOrderId(), event.getReason());
    }

    // Event DTOs
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class InventoryReservedEvent {
        private Long orderId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class InventoryReservationFailedEvent {
        private Long orderId;
        private String reason;
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
}
