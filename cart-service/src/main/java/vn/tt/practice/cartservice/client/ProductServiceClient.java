package vn.tt.practice.cartservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/products/{id}")
    ProductDTO getProductById(@PathVariable Long id);

    record ProductDTO(Long id, String name, java.math.BigDecimal price) {}
}
