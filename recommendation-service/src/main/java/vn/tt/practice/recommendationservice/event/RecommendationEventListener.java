package vn.tt.practice.recommendationservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.recommendationservice.config.RabbitMQConfig;
import vn.tt.practice.recommendationservice.service.RecommendationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationEventListener {

    private final RecommendationService recommendationService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Received OrderCompletedEvent for orderId: {}, userId: {}", 
                event.getOrderId(), event.getUserId());
        
        if (event.getItems() != null) {
            for (OrderCompletedEvent.OrderItemEvent item : event.getItems()) {
                try {
                    recommendationService.recordPurchase(
                            event.getUserId(),
                            item.getProductId(),
                            item.getCategoryId()
                    );
                    log.info("Recorded purchase for userId: {}, productId: {}", 
                            event.getUserId(), item.getProductId());
                } catch (Exception e) {
                    log.error("Failed to record purchase for userId: {}, productId: {}", 
                            event.getUserId(), item.getProductId(), e);
                }
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_VIEWED_QUEUE)
    public void handleProductViewed(ProductViewedEvent event) {
        log.info("Received ProductViewedEvent for productId: {}, userId: {}", 
                event.getProductId(), event.getUserId());
        
        try {
            recommendationService.recordView(
                    event.getUserId(),
                    event.getProductId(),
                    event.getCategoryId()
            );
        } catch (Exception e) {
            log.error("Failed to record view for userId: {}, productId: {}", 
                    event.getUserId(), event.getProductId(), e);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class OrderCompletedEvent {
        private Long orderId;
        private Long userId;
        private java.math.BigDecimal totalAmount;
        private java.util.List<OrderItemEvent> items;

        @lombok.Data
        @lombok.AllArgsConstructor
        @lombok.NoArgsConstructor
        @lombok.Builder
        public static class OrderItemEvent {
            private Long productId;
            private Long categoryId;
            private Integer quantity;
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class ProductViewedEvent {
        private Long userId;
        private Long productId;
        private Long categoryId;
    }
}
