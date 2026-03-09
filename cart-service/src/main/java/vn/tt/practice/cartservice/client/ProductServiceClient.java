package vn.tt.practice.cartservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.tt.practice.cartservice.dto.ProductCartInfoDTO;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @GetMapping("/products/{id}/cart-info")
    ProductCartInfoDTO getProductCartInfo(@PathVariable("id") Long id);
}
