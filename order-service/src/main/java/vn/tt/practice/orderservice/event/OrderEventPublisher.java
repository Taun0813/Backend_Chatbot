package vn.tt.practice.orderservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.orderservice.config.RabbitMQConfig;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.model.OrderItem;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        publishEvent("CREATED", order);
    }

    public void publishOrderReserved(Order order) {
        publishEvent("RESERVED", order);
    }

    public void publishOrderPaid(Order order) {
        publishEvent("PAID", order);
    }

    public void publishOrderConfirmed(Order order) {
        publishEvent("CONFIRMED", order);
    }

    public void publishOrderCancelled(Order order) {
        publishEvent("CANCELLED", order);
    }

    public void publishOrderFailed(Order order) {
        publishEvent("FAILED", order);
    }

    private void publishEvent(String eventType, Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventType(eventType)
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(order.getItems().stream()
                        .map(item -> OrderEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        String routingKey = "order." + eventType.toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, routingKey, event);
            log.info("Published order event: {} with routing key: {}", eventType, routingKey);
        } catch (Exception e) {
            log.error("Failed to publish order event: {}", eventType, e);
        }
    }
}
