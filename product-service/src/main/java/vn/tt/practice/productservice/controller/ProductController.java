package vn.tt.practice.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "Product Controller", description = "APIs for product management")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Get a paginated list of all products")
    @GetMapping
    public Page<ProductResponse> getAll(
            @PageableDefault(size = 10)
            Pageable pageable) {
        return productService.getAll(pageable);
    }

    @Operation(summary = "Get product by ID", description = "Get details of a specific product by its ID")
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @Operation(summary = "Create product", description = "Create a new product (Admin only)")
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @Operation(summary = "Update product", description = "Update an existing product (Admin only)")
    @PatchMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id,
                                  @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @Operation(summary = "Delete product", description = "Delete a product (Admin only)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }

    @Operation(summary = "Search products", description = "Search products by name or description")
    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String query) {
        return productService.search(query);
    }

    @Operation(summary = "Get product specs", description = "Get specifications of a product")
    @GetMapping("/{id}/specs")
    public Map<String, Object> getSpecs(@PathVariable UUID id) {
        return productService.getSpecs(id);
    }
}
