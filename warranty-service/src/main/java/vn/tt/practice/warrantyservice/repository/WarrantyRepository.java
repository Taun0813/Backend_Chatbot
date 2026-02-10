package vn.tt.practice.warrantyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.warrantyservice.entity.Warranty;

import java.util.List;
import java.util.Optional;

public interface WarrantyRepository extends JpaRepository<Warranty, Long> {
    List<Warranty> findByOrderId(Long orderId);
    List<Warranty> findByUserId(Long userId);
    Optional<Warranty> findByWarrantyNumber(String warrantyNumber);
}
