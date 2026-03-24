package vn.tt.practice.productservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.tt.practice.productservice.dto.ProductCreateRequest;
import vn.tt.practice.productservice.dto.ProductDTO;
import vn.tt.practice.productservice.dto.ProductUpdateRequest;
import vn.tt.practice.productservice.entity.Product;
import vn.tt.practice.productservice.entity.ProductImage;
import vn.tt.practice.productservice.entity.ProductSpec;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDTO toDto(Product product);

    ProductDTO.ProductImageDTO toImageDto(ProductImage image);
    ProductDTO.ProductSpecDTO toSpecDto(ProductSpec spec);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "specs", ignore = true)
    Product toEntity(ProductCreateRequest request);

    ProductImage toImageEntity(ProductCreateRequest.ProductImageRequest request);
    ProductSpec toSpecEntity(ProductCreateRequest.ProductSpecRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "specs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(ProductUpdateRequest request, @MappingTarget Product product);
}
