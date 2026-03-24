package vn.tt.practice.warrantyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.warrantyservice.dto.*;
import vn.tt.practice.warrantyservice.enums.ClaimStatus;
import vn.tt.practice.warrantyservice.enums.WarrantyStatus;
import vn.tt.practice.warrantyservice.entity.Warranty;
import vn.tt.practice.warrantyservice.entity.WarrantyClaim;
import vn.tt.practice.warrantyservice.event.WarrantyEventPublisher;
import vn.tt.practice.warrantyservice.exception.WarrantyExpiredException;
import vn.tt.practice.warrantyservice.exception.WarrantyNotFoundException;
import vn.tt.practice.warrantyservice.repository.WarrantyClaimRepository;
import vn.tt.practice.warrantyservice.repository.WarrantyRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final WarrantyClaimRepository claimRepository;
    private final WarrantyEventPublisher eventPublisher;

    private static final int DEFAULT_WARRANTY_PERIOD_MONTHS = 12;

    @Override
    public WarrantyDTO createWarranty(Long orderId, Long productId, Long userId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(DEFAULT_WARRANTY_PERIOD_MONTHS);

        Warranty warranty = Warranty.builder()
                .orderId(orderId)
                .productId(productId)
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .warrantyPeriodMonths(DEFAULT_WARRANTY_PERIOD_MONTHS)
                .status(WarrantyStatus.ACTIVE)
                .build();

        Warranty savedWarranty = warrantyRepository.save(warranty);
        log.info("Created warranty {} for orderId: {}, productId: {}", 
                savedWarranty.getWarrantyNumber(), orderId, productId);

        return toDTO(savedWarranty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyDTO> getWarrantiesByOrderId(Long orderId) {
        return warrantyRepository.findByOrderId(orderId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyDTO getWarrantyById(Long id) {
        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new WarrantyNotFoundException("Warranty not found with id: " + id));
        return toDTO(warranty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyDTO> getMyWarranties(Long userId) {
        return warrantyRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarrantyClaimDTO submitClaim(Long userId, WarrantyClaimRequest request) {
        Warranty warranty = warrantyRepository.findById(request.getWarrantyId())
                .orElseThrow(() -> new WarrantyNotFoundException("Warranty not found: " + request.getWarrantyId()));

        if (!warranty.getUserId().equals(userId)) {
            throw new WarrantyNotFoundException("Warranty does not belong to user");
        }

        if (warranty.getStatus() != WarrantyStatus.ACTIVE) {
            throw new WarrantyExpiredException("Warranty is not active. Status: " + warranty.getStatus());
        }

        if (LocalDate.now().isAfter(warranty.getEndDate())) {
            warranty.setStatus(WarrantyStatus.EXPIRED);
            warrantyRepository.save(warranty);
            throw new WarrantyExpiredException("Warranty has expired");
        }

        WarrantyClaim claim = WarrantyClaim.builder()
                .warranty(warranty)
                .userId(userId)
                .description(request.getDescription())
                .status(ClaimStatus.PENDING)
                .build();

        WarrantyClaim savedClaim = claimRepository.save(claim);
        log.info("Submitted warranty claim {} for warranty: {}", 
                savedClaim.getClaimNumber(), warranty.getWarrantyNumber());

        return toClaimDTO(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyClaimDTO getClaimById(Long id) {
        WarrantyClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new WarrantyNotFoundException("Warranty claim not found: " + id));
        return toClaimDTO(claim);
    }

    @Override
    public WarrantyClaimDTO updateClaimStatus(Long id, ClaimStatus status, String resolution) {
        WarrantyClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new WarrantyNotFoundException("Warranty claim not found: " + id));

        claim.setStatus(status);
        claim.setResolution(resolution);
        if (status == ClaimStatus.COMPLETED || status == ClaimStatus.REJECTED) {
            claim.setResolvedAt(Instant.now());
        }

        WarrantyClaim updatedClaim = claimRepository.save(claim);

        // Publish event
        eventPublisher.publishClaimStatusChanged(updatedClaim);

        return toClaimDTO(updatedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarrantyClaimDTO> getAllClaims(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.findAll(pageable).map(this::toClaimDTO);
    }

    private WarrantyDTO toDTO(Warranty warranty) {
        return WarrantyDTO.builder()
                .id(warranty.getId())
                .orderId(warranty.getOrderId())
                .productId(warranty.getProductId())
                .userId(warranty.getUserId())
                .warrantyNumber(warranty.getWarrantyNumber())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .warrantyPeriodMonths(warranty.getWarrantyPeriodMonths())
                .status(warranty.getStatus())
                .createdAt(warranty.getCreatedAt())
                .updatedAt(warranty.getUpdatedAt())
                .build();
    }

    private WarrantyClaimDTO toClaimDTO(WarrantyClaim claim) {
        return WarrantyClaimDTO.builder()
                .id(claim.getId())
                .warrantyId(claim.getWarranty().getId())
                .userId(claim.getUserId())
                .claimNumber(claim.getClaimNumber())
                .description(claim.getDescription())
                .status(claim.getStatus())
                .resolution(claim.getResolution())
                .submittedAt(claim.getSubmittedAt())
                .resolvedAt(claim.getResolvedAt())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }
}
