package vn.tt.practice.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.productservice.dto.*;
import vn.tt.practice.productservice.service.ProductService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final ProductService productService;

    private static Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) return Set.of();
        return Stream.of(rolesHeader.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static boolean hasAdminOrSuperAdmin(Set<String> roles) {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_SUPER_ADMIN);
    }

    @GetMapping
    @Operation(summary = "Get all products (paginated)")
    public ResponseEntity<PageResponse<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<PageResponse<ProductDTO>> searchProducts(@ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(productService.searchProducts(request));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PageResponse<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @PostMapping
    @Operation(summary = "Create product (ADMIN)")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product (ADMIN)")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (ADMIN)")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
