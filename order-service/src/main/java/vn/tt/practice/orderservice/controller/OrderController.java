package vn.tt.practice.orderservice.controller;

import vn.tt.practice.orderservice.dto.Request;
import vn.tt.practice.orderservice.dto.Response;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.repository.OrderRepository;
import vn.tt.practice.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    //Mock User when testing
    private UUID getUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @PostMapping
    public Response create(
            @RequestBody Request request
    ) {
        return orderService.createOrder(getUserId(), request);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
