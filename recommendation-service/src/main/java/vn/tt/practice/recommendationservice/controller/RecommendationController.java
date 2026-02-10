package vn.tt.practice.recommendationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.recommendationservice.dto.RecommendationDTO;
import vn.tt.practice.recommendationservice.dto.UserPreferenceDTO;
import vn.tt.practice.recommendationservice.enums.RecommendationType;
import vn.tt.practice.recommendationservice.service.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation Management", description = "APIs for product recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    private Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) {
            return Long.parseLong(userIdHeader);
        }
        return null; // Allow anonymous recommendations
    }

    @GetMapping("/{type}")
    @Operation(summary = "Get recommendations by type")
    public ResponseEntity<List<RecommendationDTO>> getRecommendations(
            @PathVariable RecommendationType type,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(recommendationService.getRecommendations(userId, type, limit));
    }

    @GetMapping("/product/{productId}/related")
    @Operation(summary = "Get related products")
    public ResponseEntity<List<RecommendationDTO>> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getRelatedProducts(productId, limit));
    }

    @GetMapping("/user/preferences")
    @Operation(summary = "Get user preferences")
    public ResponseEntity<List<UserPreferenceDTO>> getUserPreferences(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(recommendationService.getUserPreferences(userId));
    }

    @PostMapping("/refresh/{type}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    @Operation(summary = "Refresh recommendations (ADMIN)")
    public ResponseEntity<Void> refreshRecommendations(@PathVariable RecommendationType type) {
        recommendationService.refreshRecommendations(type);
        return ResponseEntity.ok().build();
    }
}
