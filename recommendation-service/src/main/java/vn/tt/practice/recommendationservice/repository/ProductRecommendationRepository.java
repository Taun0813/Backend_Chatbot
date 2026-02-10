package vn.tt.practice.recommendationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tt.practice.recommendationservice.entity.ProductRecommendation;
import vn.tt.practice.recommendationservice.enums.RecommendationType;

import java.util.List;

public interface ProductRecommendationRepository extends JpaRepository<ProductRecommendation, Long> {
    List<ProductRecommendation> findByUserIdAndRecommendationTypeOrderByRankPositionAsc(
            Long userId, RecommendationType type);
    
    List<ProductRecommendation> findByRecommendationTypeAndUserIdIsNullOrderByRankPositionAsc(
            RecommendationType type);
    
    @Query("SELECT pr FROM ProductRecommendation pr WHERE pr.productId = :productId " +
           "AND pr.recommendationType = :type ORDER BY pr.score DESC")
    List<ProductRecommendation> findByProductIdAndType(Long productId, RecommendationType type);
    
    void deleteByUserIdAndRecommendationType(Long userId, RecommendationType type);
}
