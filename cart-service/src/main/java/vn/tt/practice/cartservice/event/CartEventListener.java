package vn.tt.practice.cartservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.cartservice.config.RabbitMQConfig;
import vn.tt.practice.cartservice.service.CartService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventListener {

    private final CartService cartService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        cartService.clearCart(event.getUserId());
    }

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_DELETED_QUEUE)
    public void handleProductDeleted(ProductDeletedEvent event) {
        cartService.removeProductFromCarts(event.getProductId());
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class OrderCreatedEvent {
        private String eventId;
        private Long orderId;
        private Long userId;
        private Instant occurredAt;
    }


    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ProductDeletedEvent {
        private Long productId;
    }
}
