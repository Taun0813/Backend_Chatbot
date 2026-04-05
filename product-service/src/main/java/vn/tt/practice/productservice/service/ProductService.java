package vn.tt.practice.productservice.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.productservice.dto.*;
import vn.tt.practice.productservice.entity.Category;
import vn.tt.practice.productservice.entity.Product;
import vn.tt.practice.productservice.entity.ProductImage;
import vn.tt.practice.productservice.entity.ProductSpec;
import vn.tt.practice.productservice.event.ProductEventPublisher;
import vn.tt.practice.productservice.exception.CategoryNotFoundException;
import vn.tt.practice.productservice.exception.ProductNotFoundException;
import vn.tt.practice.productservice.mapper.ProductMapper;
import vn.tt.practice.productservice.repository.CategoryRepository;
import vn.tt.practice.productservice.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService  {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Cacheable(value = "productList", key = "'all_' + #page + '_' + #size")
    public PageResponse<ProductDTO> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Product> productPage = productRepository.findAll(pageable);
        return mapToPageResponse(productPage);
    }

//    @Transactional(readOnly = true)
//    public PageResponse<ProductDTO> getAllProducts(
//            int page,
//            int size,
//            String keyword,
//            String brand,
//            Long categoryId,
//            BigDecimal minPrice,
//            BigDecimal maxPrice,
//            Boolean isActive
//    ) {
//        Pageable pageable = PageRequest.of(page, size);
//
//        Specification<Product> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (keyword != null && !keyword.isBlank()) {
//                String pattern = "%" + keyword.trim().toLowerCase() + "%";
//                predicates.add(
//                        cb.like(cb.lower(root.get("name")), pattern)
//                );
//            }
//
//            if (brand != null && !brand.isBlank()) {
//                predicates.add(
//                        cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase())
//                );
//            }
//
//            if (categoryId != null) {
//                predicates.add(
//                        cb.equal(root.get("category").get("id"), categoryId)
//                );
//            }
//
//            if (minPrice != null) {
//                predicates.add(
//                        cb.greaterThanOrEqualTo(root.get("price").as(BigDecimal.class), minPrice)
//                );
//            }
//
//            if (maxPrice != null) {
//                predicates.add(
//                        cb.lessThanOrEqualTo(root.get("price").as(BigDecimal.class), maxPrice)
//                );
//            }
//
//            if (isActive != null) {
//                predicates.add(
//                        cb.equal(root.get("isActive"), isActive)
//                );
//            }
//
//            return predicates.isEmpty()
//                    ? cb.conjunction()
//                    : cb.and(predicates.toArray(new Predicate[0]));
//        };
//
//        Page<Product> productPage = productRepository.findAll(spec, pageable);
//        return mapToPageResponse(productPage);
//    }

    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> searchProducts(ProductSearchRequest request) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDir()),
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Product> spec = buildProductSearchSpecification(request);

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        return mapToPageResponse(productPage);
    }

    private Specification<Product> buildProductSearchSpecification(ProductSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern),
                                cb.like(cb.lower(root.get("brand")), pattern),
                                cb.like(cb.lower(root.get("model")), pattern)
                        )
                );
            }

            if (request.getBrand() != null && !request.getBrand().isBlank()) {
                predicates.add(
                        cb.equal(cb.lower(root.get("brand")), request.getBrand().trim().toLowerCase())
                );
            }

            if (request.getCategoryId() != null) {
                predicates.add(
                        cb.equal(root.get("category").get("id"), request.getCategoryId())
                );
            }

            if (request.getMinPrice() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice())
                );
            }

            if (request.getMaxPrice() != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productList", key = "'cat_' + #categoryId + '_' + #page + '_' + #size")
    public PageResponse<ProductDTO> getProductsByCategory(Long categoryId, int page, int size) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
        return mapToPageResponse(productPage);
    }

    @Transactional
    @CacheEvict(value = "productList", allEntries = true)
    public ProductDTO createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        if (request.getImages() != null) {
            request.getImages().forEach(imgReq -> {
                ProductImage image = productMapper.toImageEntity(imgReq);
                product.addImage(image);
            });
        }

        if (request.getSpecs() != null) {
            request.getSpecs().forEach(specReq -> {
                ProductSpec spec = productMapper.toSpecEntity(specReq);
                product.addSpec(spec);
            });
        }

        Product savedProduct = productRepository.save(product);
        eventPublisher.publishProductCreated(savedProduct.getId(), savedProduct.getName(), savedProduct.getCategory().getId());
        
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public ProductDTO updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        productMapper.updateEntityFromRequest(request, product);

        if (request.getImages() != null) {
            product.getImages().clear();
            request.getImages().forEach(imgReq -> {
                ProductImage image = productMapper.toImageEntity(imgReq);
                product.addImage(image);
            });
        }

        if (request.getSpecs() != null) {
            product.getSpecs().clear();
            request.getSpecs().forEach(specReq -> {
                ProductSpec spec = productMapper.toSpecEntity(specReq);
                product.addSpec(spec);
            });
        }

        Product updatedProduct = productRepository.save(product);
        eventPublisher.publishProductUpdated(updatedProduct.getId(), updatedProduct.getName(), updatedProduct.getCategory().getId());

        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "productList", allEntries = true)
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        eventPublisher.publishProductDeleted(id);
    }

    private PageResponse<ProductDTO> mapToPageResponse(Page<Product> productPage) {
        List<ProductDTO> content = productPage.getContent().stream()
                .map(productMapper::toDto)
                .toList();
        
        return new PageResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ProductCartInfoDTO getProductCartInfo(Long id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        ProductCartInfoDTO dto = new ProductCartInfoDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setIsActive(product.getIsActive());
        dto.setImageUrl(extractPrimaryImageUrl(product));
        return dto;
    }

    private String extractPrimaryImageUrl(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }

        return product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElseGet(() ->
                        product.getImages().stream()
                                .sorted(Comparator.comparing(
                                        ProductImage::getDisplayOrder,
                                        Comparator.nullsLast(Integer::compareTo)
                                ))
                                .findFirst()
                                .map(ProductImage::getImageUrl)
                                .orElse(null)
                );
    }
}
