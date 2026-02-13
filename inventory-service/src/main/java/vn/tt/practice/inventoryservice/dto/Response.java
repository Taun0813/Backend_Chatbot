package vn.tt.practice.inventoryservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Response {
    private Boolean reserved;
    private Long reservationId;
    private Instant expiresAt;
    private String message;
}
