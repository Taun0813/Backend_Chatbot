package vn.tt.practice.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Response {

    private UUID orderId;
    private String status;
}
