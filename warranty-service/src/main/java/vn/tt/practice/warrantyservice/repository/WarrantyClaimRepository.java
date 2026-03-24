package vn.tt.practice.warrantyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.warrantyservice.entity.WarrantyClaim;

import java.util.List;
import java.util.Optional;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    List<WarrantyClaim> findByWarrantyId(Long warrantyId);
    List<WarrantyClaim> findByUserId(Long userId);
    Optional<WarrantyClaim> findByClaimNumber(String claimNumber);
}
