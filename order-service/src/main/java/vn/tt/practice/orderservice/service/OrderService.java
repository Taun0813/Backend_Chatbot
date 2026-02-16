package vn.tt.practice.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.orderservice.dto.CreateOrderRequest;
import vn.tt.practice.orderservice.dto.OrderDTO;
import vn.tt.practice.orderservice.dto.OrderItemDTO;
import vn.tt.practice.orderservice.dto.OrderStatusHistoryDTO;
import vn.tt.practice.orderservice.dto.OrderStatusUpdateRequest;
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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName() != null ? item.getProductName() : "Product " + item.getProductId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        addStatusHistory(savedOrder, OrderStatus.PENDING, "Order created");

        eventPublisher.publishOrderCreated(savedOrder);

        return mapToDTO(savedOrder);
    }

    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return mapToDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.setStatus(request.getStatus());
        order = orderRepository.save(order);
        addStatusHistory(order, request.getStatus(), request.getNotes() != null ? request.getNotes() : "Status updated");
        return mapToDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.RESERVED) {
            throw new InvalidOrderStatusException("Order cannot be cancelled in status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        addStatusHistory(order, OrderStatus.CANCELLED, "Order cancelled");
        eventPublisher.publishOrderCancelled(order);
        return mapToDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDTO> getOrderStatusHistory(Long orderId) {
        return statusHistoryRepository.findByOrder_IdOrderByCreatedAtAsc(orderId).stream()
                .map(h -> OrderStatusHistoryDTO.builder()
                        .id(h.getId())
                        .orderId(h.getOrder().getId())
                        .status(h.getStatus())
                        .notes(h.getNotes())
                        .createdAt(h.getCreatedAt())
                        .createdBy(h.getCreatedBy())
                        .build())
                .collect(Collectors.toList());
    }

    private void addStatusHistory(Order order, OrderStatus status, String notes) {
        statusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes(notes)
                .build());
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
