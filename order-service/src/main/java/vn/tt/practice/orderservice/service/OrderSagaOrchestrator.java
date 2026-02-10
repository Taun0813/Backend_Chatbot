package vn.tt.practice.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.orderservice.enums.OrderStatus;
import vn.tt.practice.orderservice.model.Order;
import vn.tt.practice.orderservice.model.OrderStatusHistory;
import vn.tt.practice.orderservice.repository.OrderRepository;
import vn.tt.practice.orderservice.repository.OrderStatusHistoryRepository;
import vn.tt.practice.orderservice.event.OrderEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public void handleInventoryReserved(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.RESERVED);
            orderRepository.save(order);
            addStatusHistory(order, OrderStatus.RESERVED, "Inventory reserved successfully");
            eventPublisher.publishOrderReserved(order);
            log.info("Order {} status updated to RESERVED", orderId);
        }
    }

    @Transactional
    public void handleInventoryReservationFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        addStatusHistory(order, OrderStatus.FAILED, "Inventory reservation failed: " + reason);
        eventPublisher.publishOrderFailed(order);
        log.warn("Order {} failed due to inventory reservation: {}", orderId, reason);
    }

    @Transactional
    public void handlePaymentCompleted(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.RESERVED || order.getStatus() == OrderStatus.PAYMENT_PENDING) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            addStatusHistory(order, OrderStatus.PAID, "Payment completed");
            eventPublisher.publishOrderPaid(order);

            // Auto-confirm after payment
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            addStatusHistory(order, OrderStatus.CONFIRMED, "Order confirmed");
            eventPublisher.publishOrderConfirmed(order);
            log.info("Order {} status updated to CONFIRMED after payment", orderId);
        }
    }

    @Transactional
    public void handlePaymentFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        addStatusHistory(order, OrderStatus.FAILED, "Payment failed: " + reason);
        eventPublisher.publishOrderFailed(order);
        log.warn("Order {} failed due to payment: {}", orderId, reason);
    }

    private void addStatusHistory(Order order, OrderStatus status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes(notes)
                .build();
        statusHistoryRepository.save(history);
    }
}
