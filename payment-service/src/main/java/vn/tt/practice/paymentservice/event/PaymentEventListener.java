package vn.tt.practice.paymentservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.paymentservice.dto.PaymentRequest;
import vn.tt.practice.paymentservice.enums.PaymentMethod;
import vn.tt.practice.paymentservice.exception.PaymentFailedException;
import vn.tt.practice.paymentservice.service.PaymentService;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "order.reserved")
    public void handleOrderReserved(OrderReservedEvent event) {
        log.info("Received OrderReservedEvent for orderId: {}, amount: {}", event.getOrderId(), event.getTotalAmount());
        PaymentRequest request = PaymentRequest.builder()
                .orderId(event.getOrderId())
                .amount(event.getTotalAmount() != null ? event.getTotalAmount() : BigDecimal.ZERO)
                .paymentMethod(event.getPaymentMethod() != null ? event.getPaymentMethod() : PaymentMethod.BANK_TRANSFER)
                .build();
        try {
            paymentService.processPayment(request);
        } catch (PaymentFailedException e) {
            log.warn("Payment failed for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class OrderReservedEvent {
        private Long orderId;
        private BigDecimal totalAmount;
        private PaymentMethod paymentMethod;
    }
}
