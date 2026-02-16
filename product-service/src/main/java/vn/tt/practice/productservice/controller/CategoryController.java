package vn.tt.practice.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.productservice.dto.CategoryDTO;
import vn.tt.practice.productservice.service.CategoryService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController {

    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final CategoryService categoryService;

    private static Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) return Set.of();
        return Stream.of(rolesHeader.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static boolean hasAdminOrSuperAdmin(Set<String> roles) {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_SUPER_ADMIN);
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    @Operation(summary = "Create category (ADMIN)")
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return new ResponseEntity<>(categoryService.createCategory(categoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category (ADMIN)")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category (ADMIN)")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
