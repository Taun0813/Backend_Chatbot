CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    category_id BIGINT,
    view_count INT NOT NULL DEFAULT 0,
    purchase_count INT NOT NULL DEFAULT 0,
    last_viewed_at TIMESTAMP,
    last_purchased_at TIMESTAMP,
    preference_score DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

CREATE TABLE product_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT NOT NULL,
    recommendation_type VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    rank_position INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_preferences_user ON user_preferences(user_id);
CREATE INDEX idx_user_preferences_product ON user_preferences(product_id);
CREATE INDEX idx_user_preferences_category ON user_preferences(category_id);
CREATE INDEX idx_user_preferences_score ON user_preferences(preference_score DESC);

CREATE INDEX idx_recommendations_user_type ON product_recommendations(user_id, recommendation_type);
CREATE INDEX idx_recommendations_product ON product_recommendations(product_id);
CREATE INDEX idx_recommendations_type_rank ON product_recommendations(recommendation_type, rank_position);
