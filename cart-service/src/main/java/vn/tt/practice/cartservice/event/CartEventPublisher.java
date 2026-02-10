package vn.tt.practice.cartservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.cartservice.config.RabbitMQConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCartItemAdded(Long userId, Long productId) {
        CartItemEvent event = new CartItemEvent(userId, productId, "ADDED");
        String routingKey = "cart.item.added";
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.DOMAIN_EXCHANGE, routingKey, event);
            log.info("Published cart item added event for userId: {}, productId: {}", userId, productId);
        } catch (Exception e) {
            log.error("Failed to publish cart item added event", e);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CartItemEvent {
        private Long userId;
        private Long productId;
        private String action;
    }
}
