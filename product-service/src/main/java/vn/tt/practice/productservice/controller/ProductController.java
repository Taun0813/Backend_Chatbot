package vn.tt.practice.productservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.productservice.dto.ProductRequest;
import vn.tt.practice.productservice.dto.ProductResponse;
import vn.tt.practice.productservice.service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id,
                                  @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String query) {
        return productService.search(query);
    }

    @GetMapping("/{id}/specs")
    public Map<String, Object> getSpecs(@PathVariable UUID id) {
        return productService.getSpecs(id);
    }
}

