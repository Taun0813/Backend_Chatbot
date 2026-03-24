package vn.tt.practice.warrantyservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import vn.tt.practice.warrantyservice.config.RabbitMQConfig;
import vn.tt.practice.warrantyservice.entity.WarrantyClaim;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarrantyEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishClaimStatusChanged(WarrantyClaim claim) {
        ClaimStatusChangedEvent event = new ClaimStatusChangedEvent(
                claim.getId(),
                claim.getWarranty().getId(),
                claim.getUserId(),
                claim.getStatus().name()
        );
        String routingKey = "warranty.claim.status.changed";
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.WARRANTY_EXCHANGE, routingKey, event);
            log.info("Published claim status changed event for claimId: {}", claim.getId());
        } catch (Exception e) {
            log.error("Failed to publish claim status changed event", e);
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ClaimStatusChangedEvent {
        private Long claimId;
        private Long warrantyId;
        private Long userId;
        private String status;
    }
}
