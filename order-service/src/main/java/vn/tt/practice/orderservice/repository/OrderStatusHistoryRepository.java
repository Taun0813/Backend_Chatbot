package vn.tt.practice.orderservice.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tt.practice.orderservice.dto.OrderStatusHistoryDTO;
import vn.tt.practice.orderservice.entity.OrderStatusHistory;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query("""
        select new vn.tt.practice.orderservice.dto.OrderStatusHistoryDTO(
            h.id,
            h.order.id,
            h.status,
            h.notes,
            h.createdAt,
            h.createdBy
        )
        from OrderStatusHistory h
        where h.order.id = :orderId
        order by h.createdAt asc
    """)
    List<OrderStatusHistoryDTO> findHistoryDto(@Param("orderId") Long orderId);
}
