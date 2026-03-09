package vn.tt.practice.inventoryservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.inventoryservice.config.RabbitMQConfig;
import vn.tt.practice.inventoryservice.dto.InventoryReservationFailedEvent;
import vn.tt.practice.inventoryservice.dto.InventoryReservedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInventoryReserved(Long orderId) {
        InventoryReservedEvent event = new InventoryReservedEvent(orderId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVED_ROUTING_KEY,
                    event
            );
            log.info("Published InventoryReservedEvent: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to publish InventoryReservedEvent", e);
            throw e;
        }
    }

    public void publishInventoryReservationFailed(Long orderId, String reason) {
        InventoryReservationFailedEvent event = new InventoryReservationFailedEvent(orderId, reason);

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
}
