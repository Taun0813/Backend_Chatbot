package vn.tt.practice.recommendationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
@Entity
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "purchase_count", nullable = false)
    @Builder.Default
    private Integer purchaseCount = 0;

    @Column(name = "last_viewed_at")
    private Instant lastViewedAt;

    @Column(name = "last_purchased_at")
    private Instant lastPurchasedAt;

    @Column(name = "preference_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal preferenceScore = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
        calculatePreferenceScore();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        calculatePreferenceScore();
    }

    private void calculatePreferenceScore() {
        // Simple scoring: views * 0.1 + purchases * 1.0
        BigDecimal viewsScore = BigDecimal.valueOf(viewCount).multiply(BigDecimal.valueOf(0.1d));
        BigDecimal purchaseScore = BigDecimal.valueOf(purchaseCount);
        this.preferenceScore = viewsScore.add(purchaseScore).setScale(2, RoundingMode.HALF_UP);
    }
}
