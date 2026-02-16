package vn.tt.practice.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.orderservice.model.OrderStatusHistory;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrder_IdOrderByCreatedAtAsc(Long orderId);
}
