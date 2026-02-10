package vn.tt.practice.recommendationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.tt.practice.recommendationservice.entity.UserPreference;

import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserIdAndProductId(Long userId, Long productId);
    List<UserPreference> findByUserIdOrderByPreferenceScoreDesc(Long userId);
    List<UserPreference> findByCategoryIdOrderByPreferenceScoreDesc(Long categoryId);
    List<UserPreference> findByProductId(Long productId);
    
    @Query("SELECT up FROM UserPreference up WHERE up.userId = :userId ORDER BY up.preferenceScore DESC")
    List<UserPreference> findTopByUserIdOrderByPreferenceScoreDesc(Long userId);
}
