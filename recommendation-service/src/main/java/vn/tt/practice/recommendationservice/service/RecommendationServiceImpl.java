package vn.tt.practice.recommendationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.recommendationservice.client.ProductServiceClient;
import vn.tt.practice.recommendationservice.dto.RecommendationDTO;
import vn.tt.practice.recommendationservice.dto.UserPreferenceDTO;
import vn.tt.practice.recommendationservice.entity.ProductRecommendation;
import vn.tt.practice.recommendationservice.entity.UserPreference;
import vn.tt.practice.recommendationservice.enums.RecommendationType;
import vn.tt.practice.recommendationservice.repository.ProductRecommendationRepository;
import vn.tt.practice.recommendationservice.repository.UserPreferenceRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final ProductRecommendationRepository recommendationRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "recommendations", key = "#userId + '_' + #type + '_' + #limit")
    public List<RecommendationDTO> getRecommendations(Long userId, RecommendationType type, int limit) {
        log.info("Getting {} recommendations for userId: {}, type: {}", limit, userId, type);

        List<ProductRecommendation> recommendations;
        if (userId != null) {
            recommendations = recommendationRepository
                    .findByUserIdAndRecommendationTypeOrderByRankPositionAsc(userId, type);
        } else {
            recommendations = recommendationRepository
                    .findByRecommendationTypeAndUserIdIsNullOrderByRankPositionAsc(type);
        }

        if (recommendations.isEmpty()) {
            // Generate recommendations on the fly
            refreshRecommendations(type);
            if (userId != null) {
                recommendations = recommendationRepository
                        .findByUserIdAndRecommendationTypeOrderByRankPositionAsc(userId, type);
            } else {
                recommendations = recommendationRepository
                        .findByRecommendationTypeAndUserIdIsNullOrderByRankPositionAsc(type);
            }
        }

        return recommendations.stream()
                .limit(limit)
                .map(this::toRecommendationDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationDTO> getRelatedProducts(Long productId, int limit) {
        log.info("Getting {} related products for productId: {}", limit, productId);

        // Get user preferences for this product to find similar users
        List<UserPreference> preferences = userPreferenceRepository.findByProductId(productId);
        
        // Find products frequently viewed/purchased together
        Set<Long> relatedProductIds = new HashSet<>();
        for (UserPreference pref : preferences) {
            List<UserPreference> userPrefs = userPreferenceRepository
                    .findByUserIdOrderByPreferenceScoreDesc(pref.getUserId());
            userPrefs.stream()
                    .filter(p -> !p.getProductId().equals(productId))
                    .limit(5)
                    .forEach(p -> relatedProductIds.add(p.getProductId()));
        }

        return relatedProductIds.stream()
                .limit(limit)
                .map(this::getProductRecommendation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void recordView(Long userId, Long productId, Long categoryId) {
        UserPreference preference = userPreferenceRepository
                .findByUserIdAndProductId(userId, productId)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .productId(productId)
                        .categoryId(categoryId)
                        .viewCount(0)
                        .purchaseCount(0)
                        .build());

        preference.setViewCount(preference.getViewCount() + 1);
        preference.setLastViewedAt(Instant.now());
        userPreferenceRepository.save(preference);

        log.debug("Recorded view for userId: {}, productId: {}", userId, productId);
    }

    @Override
    public void recordPurchase(Long userId, Long productId, Long categoryId) {
        UserPreference preference = userPreferenceRepository
                .findByUserIdAndProductId(userId, productId)
                .orElse(UserPreference.builder()
                        .userId(userId)
                        .productId(productId)
                        .categoryId(categoryId)
                        .viewCount(0)
                        .purchaseCount(0)
                        .build());

        preference.setPurchaseCount(preference.getPurchaseCount() + 1);
        preference.setLastPurchasedAt(Instant.now());
        userPreferenceRepository.save(preference);

        // Refresh personalized recommendations
        refreshRecommendations(RecommendationType.PERSONALIZED);

        log.debug("Recorded purchase for userId: {}, productId: {}", userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPreferenceDTO> getUserPreferences(Long userId) {
        return userPreferenceRepository.findByUserIdOrderByPreferenceScoreDesc(userId).stream()
                .map(this::toUserPreferenceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void refreshRecommendations(RecommendationType type) {
        log.info("Refreshing recommendations for type: {}", type);

        switch (type) {
            case POPULAR -> refreshPopularRecommendations();
            case PERSONALIZED -> refreshPersonalizedRecommendations();
            case TRENDING -> refreshTrendingRecommendations();
            case RELATED -> {
                // Related recommendations are generated on-demand
            }
        }
    }

    private void refreshPopularRecommendations() {
        // Get products with highest purchase counts
        List<UserPreference> popular = userPreferenceRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        UserPreference::getProductId,
                        Collectors.summingInt(UserPreference::getPurchaseCount)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(50)
                .map(e -> {
                    UserPreference pref = userPreferenceRepository
                            .findByUserIdAndProductId(1L, e.getKey())
                            .orElse(null);
                    if (pref == null) {
                        pref = UserPreference.builder()
                                .productId(e.getKey())
                                .purchaseCount(e.getValue())
                                .build();
                    }
                    return pref;
                })
                .collect(Collectors.toList());

        recommendationRepository.deleteByUserIdAndRecommendationType(null, RecommendationType.POPULAR);

        int rank = 1;
        for (UserPreference pref : popular) {
            ProductRecommendation rec = ProductRecommendation.builder()
                    .productId(pref.getProductId())
                    .recommendationType(RecommendationType.POPULAR)
                    .score(pref.getPreferenceScore())
                    .rankPosition(rank++)
                    .build();
            recommendationRepository.save(rec);
        }
    }

    private void refreshPersonalizedRecommendations() {
        // Get all users and generate personalized recommendations
        Set<Long> userIds = userPreferenceRepository.findAll().stream()
                .map(UserPreference::getUserId)
                .collect(Collectors.toSet());

        for (Long userId : userIds) {
            recommendationRepository.deleteByUserIdAndRecommendationType(userId, RecommendationType.PERSONALIZED);

            List<UserPreference> userPrefs = userPreferenceRepository
                    .findByUserIdOrderByPreferenceScoreDesc(userId);

            int rank = 1;
            for (UserPreference pref : userPrefs.stream().limit(20).collect(Collectors.toList())) {
                ProductRecommendation rec = ProductRecommendation.builder()
                        .userId(userId)
                        .productId(pref.getProductId())
                        .recommendationType(RecommendationType.PERSONALIZED)
                        .score(pref.getPreferenceScore())
                        .rankPosition(rank++)
                        .build();
                recommendationRepository.save(rec);
            }
        }
    }

    private void refreshTrendingRecommendations() {
        // Products viewed/purchased in last 7 days
        Instant sevenDaysAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);
        
        List<UserPreference> trending = userPreferenceRepository.findAll().stream()
                .filter(pref -> {
                    Instant lastActivity = pref.getLastViewedAt() != null ? 
                            pref.getLastViewedAt() : pref.getLastPurchasedAt();
                    return lastActivity != null && lastActivity.isAfter(sevenDaysAgo);
                })
                .sorted(Comparator.comparing(UserPreference::getPreferenceScore).reversed())
                .limit(50)
                .collect(Collectors.toList());

        recommendationRepository.deleteByUserIdAndRecommendationType(null, RecommendationType.TRENDING);

        int rank = 1;
        for (UserPreference pref : trending) {
            ProductRecommendation rec = ProductRecommendation.builder()
                    .productId(pref.getProductId())
                    .recommendationType(RecommendationType.TRENDING)
                    .score(pref.getPreferenceScore())
                    .rankPosition(rank++)
                    .build();
            recommendationRepository.save(rec);
        }
    }

    private RecommendationDTO toRecommendationDTO(ProductRecommendation rec) {
        try {
            ProductServiceClient.ProductDTO product = productServiceClient.getProductById(rec.getProductId());
            return RecommendationDTO.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .productDescription(product.description())
                    .price(product.price())
                    .categoryId(product.categoryId())
                    .categoryName(product.categoryName())
                    .recommendationType(rec.getRecommendationType())
                    .score(rec.getScore())
                    .rankPosition(rec.getRankPosition())
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch product details for productId: {}", rec.getProductId(), e);
            return null;
        }
    }

    private RecommendationDTO getProductRecommendation(Long productId) {
        try {
            ProductServiceClient.ProductDTO product = productServiceClient.getProductById(productId);
            return RecommendationDTO.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .productDescription(product.description())
                    .price(product.price())
                    .categoryId(product.categoryId())
                    .categoryName(product.categoryName())
                    .recommendationType(RecommendationType.RELATED)
                    .score(BigDecimal.ZERO)
                    .rankPosition(0)
                    .build();
        } catch (Exception e) {
            log.error("Failed to fetch product details for productId: {}", productId, e);
            return null;
        }
    }

    private UserPreferenceDTO toUserPreferenceDTO(UserPreference pref) {
        return UserPreferenceDTO.builder()
                .id(pref.getId())
                .userId(pref.getUserId())
                .productId(pref.getProductId())
                .categoryId(pref.getCategoryId())
                .viewCount(pref.getViewCount())
                .purchaseCount(pref.getPurchaseCount())
                .lastViewedAt(pref.getLastViewedAt())
                .lastPurchasedAt(pref.getLastPurchasedAt())
                .preferenceScore(pref.getPreferenceScore())
                .createdAt(pref.getCreatedAt())
                .updatedAt(pref.getUpdatedAt())
                .build();
    }
}
