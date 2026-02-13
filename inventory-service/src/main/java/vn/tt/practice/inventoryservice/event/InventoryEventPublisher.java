package vn.tt.practice.inventoryservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.inventoryservice.config.RabbitMQConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInventoryReserved(Long orderId, Long productId, Integer quantity) {
        InventoryEvent event = InventoryEvent.builder()
                .eventType("RESERVED")
                .orderId(orderId)
                .productId(productId)
                .quantity(quantity)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                    event
            );
            log.info("Published InventoryReservedEvent: orderId={}, productId={}, quantity={}", 
                    orderId, productId, quantity);
        } catch (Exception e) {
            log.error("Failed to publish InventoryReservedEvent", e);
            throw e;
        }
    }

    public void publishInventoryReservationFailed(Long orderId, String reason) {
        InventoryEvent event = InventoryEvent.builder()
                .eventType("RESERVATION_FAILED")
                .orderId(orderId)
                .reason(reason)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVATION_FAILED_ROUTING_KEY,
                    event
            );
            log.info("Published InventoryReservationFailedEvent: orderId={}, reason={}", orderId, reason);
        } catch (Exception e) {
            log.error("Failed to publish InventoryReservationFailedEvent", e);
            throw e;
        }
    }

    public void publishStockUpdated(Long productId, Integer newQuantity) {
        InventoryEvent event = InventoryEvent.builder()
                .eventType("STOCK_UPDATED")
                .productId(productId)
                .newQuantity(newQuantity)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.STOCK_UPDATED_ROUTING_KEY,
                    event
            );
            log.info("Published StockUpdatedEvent: productId={}, newQuantity={}", productId, newQuantity);
        } catch (Exception e) {
            log.error("Failed to publish StockUpdatedEvent", e);
        }
    }
}
