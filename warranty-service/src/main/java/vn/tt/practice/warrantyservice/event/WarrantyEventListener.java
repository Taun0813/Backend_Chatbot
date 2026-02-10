package vn.tt.practice.warrantyservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.warrantyservice.config.RabbitMQConfig;
import vn.tt.practice.warrantyservice.service.WarrantyService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarrantyEventListener {

    private final WarrantyService warrantyService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PAID_QUEUE)
    public void handleOrderPaid(OrderEvent event) {
        log.info("Received OrderPaidEvent for orderId: {}, eventType: {}", event.getOrderId(), event.getEventType());
        
        // Only process PAID events
        if ("PAID".equals(event.getEventType()) && event.getItems() != null) {
            for (OrderEvent.OrderItemEvent item : event.getItems()) {
                try {
                    warrantyService.createWarranty(
                            event.getOrderId(),
                            item.getProductId(),
                            event.getUserId()
                    );
                    log.info("Created warranty for orderId: {}, productId: {}", 
                            event.getOrderId(), item.getProductId());
                } catch (Exception e) {
                    log.error("Failed to create warranty for orderId: {}, productId: {}", 
                            event.getOrderId(), item.getProductId(), e);
                }
            }
        }
    }

    // Match OrderEvent structure from order-service
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class OrderEvent {
        private String eventType;
        private Long orderId;
        private String orderNumber;
        private Long userId;
        private java.math.BigDecimal totalAmount;
        private String status;
        private java.util.List<OrderItemEvent> items;

        @lombok.Data
        @lombok.AllArgsConstructor
        @lombok.NoArgsConstructor
        @lombok.Builder
        public static class OrderItemEvent {
            private Long productId;
            private String productName;
            private Integer quantity;
            private java.math.BigDecimal unitPrice;
        }
    }
}
