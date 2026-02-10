package vn.tt.practice.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.orderservice.dto.*;
import vn.tt.practice.orderservice.enums.OrderStatus;
import vn.tt.practice.orderservice.event.OrderEventPublisher;
import vn.tt.practice.orderservice.exception.InvalidOrderStatusException;
import vn.tt.practice.orderservice.exception.OrderNotFoundException;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.model.OrderItem;
import vn.tt.practice.orderservice.model.OrderStatusHistory;
import vn.tt.practice.orderservice.repository.OrderRepository;
import vn.tt.practice.orderservice.repository.OrderStatusHistoryRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingPostalCode(request.getShippingPostalCode())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .build();

        // Create order items
        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem item = OrderItem.builder()
                            .order(order)
                            .productId(itemRequest.getProductId())
                            .productName("Product " + itemRequest.getProductId()) // TODO: Fetch from Product Service
                            .quantity(itemRequest.getQuantity())
                            .unitPrice(BigDecimal.valueOf(100000)) // TODO: Fetch from Product Service
                            .build();
                    return item;
                })
                .collect(Collectors.toList());

        order.setItems(items);

        // Calculate total
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        addStatusHistory(savedOrder, OrderStatus.PENDING, "Order created");

        // Publish OrderCreatedEvent
        eventPublisher.publishOrderCreated(savedOrder);

        return toDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (!isValidStatusTransition(order.getStatus(), request.getStatus())) {
            throw new InvalidOrderStatusException(
                    "Cannot transition from " + order.getStatus() + " to " + request.getStatus());
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);
        addStatusHistory(updatedOrder, request.getStatus(), request.getNotes());

        return toDTO(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        addStatusHistory(cancelledOrder, OrderStatus.CANCELLED, "Order cancelled by user");
        eventPublisher.publishOrderCancelled(cancelledOrder);

        return toDTO(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrderStatusHistory(Long orderId) {
        return statusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable).map(this::toDTO);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus target) {
        // Simple validation - can be enhanced
        return true; // For now, allow all transitions
    }

    private void addStatusHistory(Order order, OrderStatus status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes(notes)
                .build();
        statusHistoryRepository.save(history);
    }

    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderItemDTO.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
