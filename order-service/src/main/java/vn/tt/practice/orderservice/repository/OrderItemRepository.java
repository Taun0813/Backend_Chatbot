package vn.tt.practice.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.orderservice.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
