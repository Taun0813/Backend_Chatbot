package vn.tt.practice.productservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.productservice.config.RabbitMQConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProductCreated(Long productId, String productName, Long categoryId) {
        publishEvent("CREATED", productId, productName, categoryId);
    }

    public void publishProductUpdated(Long productId, String productName, Long categoryId) {
        publishEvent("UPDATED", productId, productName, categoryId);
    }

    public void publishProductDeleted(Long productId) {
        publishEvent("DELETED", productId, null, null);
    }

    private void publishEvent(String eventType, Long productId, String productName, Long categoryId) {
        ProductEvent event = new ProductEvent(eventType, productId, productName, categoryId);
        String routingKey = "product." + eventType.toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCT_EXCHANGE, routingKey, event);
            log.info("Published product event: {} with routing key: {}", event, routingKey);
        } catch (Exception e) {
            log.error("Failed to publish product event: {}", event, e);
        }
    }
}
