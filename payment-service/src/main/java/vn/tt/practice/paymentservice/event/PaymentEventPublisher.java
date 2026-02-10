package vn.tt.practice.paymentservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.paymentservice.config.RabbitMQConfig;
import vn.tt.practice.paymentservice.entity.Payment;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentCompleted(Payment payment) {
        publishEvent("COMPLETED", payment, null);
    }

    public void publishPaymentFailed(Payment payment, String reason) {
        publishEvent("FAILED", payment, reason);
    }

    public void publishPaymentRefunded(Payment payment) {
        publishEvent("REFUNDED", payment, null);
    }

    private void publishEvent(String eventType, Payment payment, String reason) {
        PaymentEvent event = PaymentEvent.builder()
                .eventType(eventType)
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .reason(reason)
                .build();

        String routingKey = "payment." + eventType.toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, routingKey, event);
            log.info("Published payment event: {} for orderId: {}", eventType, payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish payment event: {}", eventType, e);
        }
    }
}
