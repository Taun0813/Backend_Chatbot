package vn.tt.practice.warrantyservice.service;

import org.springframework.data.domain.Page;
import vn.tt.practice.warrantyservice.dto.WarrantyClaimDTO;
import vn.tt.practice.warrantyservice.dto.WarrantyClaimRequest;
import vn.tt.practice.warrantyservice.dto.WarrantyDTO;
import vn.tt.practice.warrantyservice.enums.ClaimStatus;

import java.util.List;

public interface WarrantyService {
    WarrantyDTO createWarranty(Long orderId, Long productId, Long userId);
    List<WarrantyDTO> getWarrantiesByOrderId(Long orderId);
    WarrantyDTO getWarrantyById(Long id);
    List<WarrantyDTO> getMyWarranties(Long userId);
    WarrantyClaimDTO submitClaim(Long userId, WarrantyClaimRequest request);
    WarrantyClaimDTO getClaimById(Long id);
    WarrantyClaimDTO updateClaimStatus(Long id, ClaimStatus status, String resolution);
    Page<WarrantyClaimDTO> getAllClaims(int page, int size);
}
