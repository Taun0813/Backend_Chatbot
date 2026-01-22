package vn.tt.practice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private UUID eventId;
    private String eventType;
    private String occurredAt;
    private String traceId;
    private T payload;

    public static <T> EventEnvelope<T> create(String eventType, String traceId, T payload) {
        return EventEnvelope.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(LocalDateTime.now().toString())
                .traceId(traceId)
                .payload(payload)
                .build();
    }
}
