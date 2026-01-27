package vn.tt.practice.productservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.tt.practice.productservice.dto.ProductRequest;
import vn.tt.practice.productservice.dto.ProductResponse;
import vn.tt.practice.productservice.model.Product;
import vn.tt.practice.productservice.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
//    private final RedisTemplate redisTemplate;

    // GET /products
    @Cacheable(
            value = "products",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // GET /products/{id}
    @Cacheable(value = "product", key = "#id")
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }

    // POST /products
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Product p = new Product();
        mapToEntity(p, request);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
        return toResponse(p);
    }

    // PUT /products/{id}
    @CachePut(value = "product", key = "#id")
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse update(UUID id, ProductRequest request) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        mapToEntity(p, request);
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);
        return toResponse(p);
    }

    // DELETE /products/{id}
    @CacheEvict(value = {"product", "products"}, key = "#id", allEntries = true)
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    // GET /products/search?query=iphone
    public List<ProductResponse> search(String query) {
        return productRepository.search(query)
                .stream().map(this::toResponse)
                .toList();
    }

    // GET /products/{id}/specs
    public Map<String, Object> getSpecs(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Map<String, Object> specs = new LinkedHashMap<>();
        specs.put("brand", p.getBrand());
        specs.put("price", p.getPrice());
        specs.put("stock", p.getStock());

        return specs;
    }

    // Helper Mapping
    private void mapToEntity(Product p, ProductRequest request) {
        p.setName(request.name());
        p.setBrand(request.brand());
        p.setPrice(request.price());
        p.setStock(request.stock());
        p.setShortDescription(request.shortDescription());
        p.setDescription(request.description());
        p.setThumbnail(request.thumbnail());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getPrice(),
                product.getStock(),
                product.getShortDescription(),
                product.getDescription(),
                product.getThumbnail()
//                product.getCreatedAt(),
//                product.getUpdatedAt()
        );
    }
}
