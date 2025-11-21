package com.example.api.products.mapper;

import com.example.api.products.dto.ProductDto;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductDto copy(ProductDto dto) {
        if (dto == null) return null;
        ProductDto copy = new ProductDto();
        copy.setId(dto.getId());
        copy.setName(dto.getName());
        copy.setCategory(dto.getCategory());
        copy.setPrice(dto.getPrice());
        copy.setDescription(dto.getDescription());
        return copy;
    }
}
