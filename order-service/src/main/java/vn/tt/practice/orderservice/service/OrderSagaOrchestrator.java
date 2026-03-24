package vn.tt.practice.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.orderservice.enums.OrderStatus;
import vn.tt.practice.orderservice.entity.Order;
import vn.tt.practice.orderservice.entity.OrderStatusHistory;
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
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Skip InventoryReserved for order {} because current status is {}", orderId, order.getStatus());
            return;
        }

        changeStatus(order, OrderStatus.RESERVED, "Inventory reserved successfully");
        eventPublisher.publishOrderReserved(order);
        log.info("Order {} status updated to RESERVED", orderId);
    }

    @Transactional
    public void handleInventoryReservationFailed(Long orderId, String reason) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.FAILED) {
            log.warn("Skip duplicate inventory reservation failed event for order {}", orderId);
            return;
        }

        if (isTerminalStatus(order.getStatus())) {
            log.warn("Skip inventory reservation failed for order {} because current status is {}", orderId, order.getStatus());
            return;
        }

        changeStatus(order, OrderStatus.FAILED, "Inventory reservation failed: " + reason);
        eventPublisher.publishOrderFailed(order);
        log.warn("Order {} failed due to inventory reservation: {}", orderId, reason);
    }

    @Transactional
    public void handlePaymentCompleted(Long orderId) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("Skip duplicate payment completed event for order {} because already CONFIRMED", orderId);
            return;
        }

        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order {} already PAID, continue confirm if needed", orderId);
        }

        if (order.getStatus() != OrderStatus.RESERVED && order.getStatus() != OrderStatus.PAYMENT_PENDING && order.getStatus() != OrderStatus.PAID) {
            log.warn("Skip PaymentCompleted for order {} because current status is {}", orderId, order.getStatus());
            return;
        }

        if (order.getStatus() != OrderStatus.PAID) {
            changeStatus(order, OrderStatus.PAID, "Payment completed");
            eventPublisher.publishOrderPaid(order);
        }

        Order latestOrder = findOrder(orderId);

        if (latestOrder.getStatus() != OrderStatus.CONFIRMED) {
            changeStatus(latestOrder, OrderStatus.CONFIRMED, "Order confirmed");
            eventPublisher.publishOrderConfirmed(latestOrder);
        }

        log.info("Order {} status updated to CONFIRMED after payment", orderId);
    }

    @Transactional
    public void handlePaymentFailed(Long orderId, String reason) {
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.FAILED) {
            log.warn("Skip duplicate payment failed event for order {}", orderId);
            return;
        }

        if (isTerminalStatus(order.getStatus())) {
            log.warn("Skip payment failed for order {} because current status is {}", orderId, order.getStatus());
            return;
        }

        changeStatus(order, OrderStatus.FAILED, "Payment failed: " + reason);
        eventPublisher.publishOrderFailed(order);
        log.warn("Order {} failed due to payment: {}", orderId, reason);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    private void changeStatus(Order order, OrderStatus newStatus, String notes) {
        if (order.getStatus() == newStatus) {
            log.warn("Order {} already in status {}, skip duplicate history", order.getId(), newStatus);
            return;
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        addStatusHistory(order, newStatus, notes);
    }

    private void addStatusHistory(Order order, OrderStatus status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .notes(notes)
                .createdBy("ORDER_SAGA")
                .build();

        statusHistoryRepository.save(history);
    }

    private boolean isTerminalStatus(OrderStatus status) {
        return status == OrderStatus.FAILED
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.CANCELLED;
    }
}
