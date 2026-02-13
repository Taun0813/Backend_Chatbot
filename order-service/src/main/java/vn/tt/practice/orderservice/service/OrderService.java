package vn.tt.practice.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.orderservice.client.InventoryClient;
import vn.tt.practice.orderservice.dto.CreateOrderRequest;
import vn.tt.practice.orderservice.dto.OrderDTO;
import vn.tt.practice.orderservice.dto.OrderItemDTO;
import vn.tt.practice.orderservice.enums.OrderStatus;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.model.OrderItem;
import vn.tt.practice.orderservice.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Build order items
        List<OrderItem> items = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .productName("Product " + item.getProductId()) // TODO: Fetch from Product Service
                        .quantity(item.getQuantity())
                        .unitPrice(BigDecimal.valueOf(100000)) // TODO: Fetch from Product Service
                        .build())
                .collect(Collectors.toList());

        // Calculate total
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingPostalCode(request.getShippingPostalCode())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        // Reserve inventory
        request.getItems().forEach(item ->
                inventoryClient.reserve(
                        new InventoryClient.ReserveRequest(
                                item.getProductId(),
                                savedOrder.getId(),
                                item.getQuantity()
                        )
                )
        );

        return mapToDTO(savedOrder);
    }

    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(OrderStatus.valueOf(order.getStatus().name()))
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
}
