package vn.tt.practice.recommendationservice.service;

import vn.tt.practice.recommendationservice.dto.RecommendationDTO;
import vn.tt.practice.recommendationservice.dto.UserPreferenceDTO;
import vn.tt.practice.recommendationservice.enums.RecommendationType;

import java.util.List;

public interface RecommendationService {
    List<RecommendationDTO> getRecommendations(Long userId, RecommendationType type, int limit);
    List<RecommendationDTO> getRelatedProducts(Long productId, int limit);
    void recordView(Long userId, Long productId, Long categoryId);
    void recordPurchase(Long userId, Long productId, Long categoryId);
    List<UserPreferenceDTO> getUserPreferences(Long userId);
    void refreshRecommendations(RecommendationType type);
}
