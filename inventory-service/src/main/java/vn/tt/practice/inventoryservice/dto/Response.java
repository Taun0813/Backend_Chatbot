package vn.tt.practice.inventoryservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class Response {
    private boolean reserved;
    private UUID reservationId;
    private Instant expiredAt;
}
