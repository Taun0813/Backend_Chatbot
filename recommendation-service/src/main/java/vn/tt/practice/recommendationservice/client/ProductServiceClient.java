package vn.tt.practice.recommendationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/products/{id}")
    ProductDTO getProductById(@PathVariable Long id);

    @GetMapping("/products")
    java.util.List<ProductDTO> getAllProducts();

    record ProductDTO(
            Long id,
            String name,
            String description,
            java.math.BigDecimal price,
            Long categoryId,
            String categoryName,
            Integer stockQuantity,
            Boolean active
    ) {}
}
