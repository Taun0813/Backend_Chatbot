package vn.tt.practice.recommendationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.recommendationservice.dto.InteractionDTO;
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

    private boolean hasAdminRole(HttpServletRequest request) {
        String roles = request.getHeader("X-User-Roles");
        if (roles == null) return false;
        return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN");
    }

    @GetMapping("/me")
    @Operation(summary = "Get personalized recommendations")
    public ResponseEntity<List<RecommendationDTO>> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(
                recommendationService.getRecommendations(userId, RecommendationType.PERSONALIZED, limit)
        );
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular products")
    public ResponseEntity<List<RecommendationDTO>> getPopularRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                recommendationService.getRecommendations(null, RecommendationType.POPULAR, limit)
        );
    }

    @GetMapping("/similar/{productId}")
    @Operation(summary = "Get similar products")
    public ResponseEntity<List<RecommendationDTO>> getSimilarProducts(
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

    @PostMapping("/track")
    @Operation(summary = "Track user interaction")
    public ResponseEntity<Void> trackInteraction(
            @RequestBody @Valid InteractionDTO interaction,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        recommendationService.trackInteraction(
                userId,
                interaction.getProductId(),
                interaction.getCategoryId(),
                interaction.getInteractionType()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh/{type}")
    @Operation(summary = "Refresh recommendations (ADMIN)")
    public ResponseEntity<Void> refreshRecommendations(
            @PathVariable RecommendationType type,
            HttpServletRequest request) {
        if (!hasAdminRole(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        recommendationService.refreshRecommendations(type);
        return ResponseEntity.ok().build();
    }
}
