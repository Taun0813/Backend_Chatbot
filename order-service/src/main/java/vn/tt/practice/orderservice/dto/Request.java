package vn.tt.practice.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Request {

    private List<Item> items;

    @Data
    public static class Item {
        private UUID productId;
        private int quantity;
    }
}
