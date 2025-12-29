package vn.tt.practice.orderservice.service;

import vn.tt.practice.orderservice.client.InventoryClient;
import vn.tt.practice.orderservice.dto.Request;
import vn.tt.practice.orderservice.dto.Response;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.model.OrderItem;
import vn.tt.practice.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public Response createOrder(
            UUID userId,
            Request request
    ) {

        UUID orderId = UUID.randomUUID();

        List<OrderItem> items = request.getItems().stream()
                .map(i -> OrderItem.builder()
                        .id(UUID.randomUUID())
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .price(100_000) // giả lập, sau này gọi Product Service
                        .build())
                .toList();

        long total = items.stream()
                .mapToLong(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(Order.Status.PENDING)
                .totalAmount(total)
                .createdAt(Instant.now())
                .items(items)
                .build();

        items.forEach(i -> i.setOrder(order));

        orderRepository.save(order);

        // reserve inventory
        request.getItems().forEach(i ->
                inventoryClient.reserve(
                        new InventoryClient.ReserveRequest(
                                i.getProductId(),
                                orderId,
                                i.getQuantity()
                        )
                )
        );

        return Response.builder()
                .orderId(orderId)
                .status(order.getStatus().name())
                .build();
    }
}
