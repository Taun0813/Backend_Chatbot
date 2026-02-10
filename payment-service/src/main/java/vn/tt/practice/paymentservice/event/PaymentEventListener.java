package vn.tt.practice.paymentservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.paymentservice.service.PaymentService;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order.reserved")
    public void handleOrderReserved(OrderReservedEvent event) {
        log.info("Received OrderReservedEvent for orderId: {}", event.getOrderId());
        // PaymentService will process payment when order is reserved
        // This is handled in the controller/service layer
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class OrderReservedEvent {
        private Long orderId;
        private BigDecimal totalAmount;
    }
}
