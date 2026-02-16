package vn.tt.practice.recommendationservice.enums;

public enum InteractionType {
    VIEW(1.0),            // Viewed product
    ADD_TO_CART(3.0),     // Added to cart
    PURCHASE(5.0),        // Purchased
    REMOVE_FROM_CART(-1.0); // Removed from cart

    private final double score;

    InteractionType(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
}

