package com.example.db.product.mapper;

import com.example.db.product.domain.Product;
import com.example.db.product.dto.*;
import org.springframework.data.domain.Page;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toEntity(ProductCreateRequest dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
        product.setDescription(dto.description());
        return product;
    }

    public static void updateEntity(Product product, ProductUpdateRequest dto) {
        product.setName(dto.name());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
        product.setDescription(dto.description());
    }

    public static ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getDescription()
        );
    }

    public static PageResponse<ProductResponseDto> toDtoPage(Page<Product> products) {
        return new PageResponse<>(
                products.getContent().stream()
                        .map(ProductMapper::toDto)
                        .toList(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }
}
