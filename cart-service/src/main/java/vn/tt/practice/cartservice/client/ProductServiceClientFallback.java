package vn.tt.practice.cartservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductDTO getProductById(Long id) {
        log.warn("ProductServiceClient fallback triggered for productId: {}", id);
        // Return a default product or throw exception based on business logic
        throw new RuntimeException("Product service unavailable. Cannot fetch product: " + id);
    }
}
