package com.example.db.product.mapper;

import com.example.db.product.domain.Product;
import com.example.db.product.dto.ProductCreateRequest;
import com.example.db.product.dto.ProductResponseDto;
import com.example.db.product.dto.ProductUpdateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    @Test
    void toEntityShouldMapAllFieldsFromCreateRequest() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Test name",
                "Category",
                new BigDecimal("19.99"),
                "Some description"
        );

        Product entity = ProductMapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Test name");
        assertThat(entity.getCategory()).isEqualTo("Category");
        assertThat(entity.getPrice()).isEqualByComparingTo("19.99");
        assertThat(entity.getDescription()).isEqualTo("Some description");
    }

    @Test
    void updateEntityShouldOverrideAllUpdatableFields() {
        Product existing = new Product();
        existing.setId(10L);
        existing.setName("Old name");
        existing.setCategory("Old category");
        existing.setPrice(new BigDecimal("5.00"));
        existing.setDescription("Old description");

        ProductUpdateRequest request = new ProductUpdateRequest(
                "New name",
                "New category",
                new BigDecimal("25.50"),
                "New description"
        );

        ProductMapper.updateEntity(existing, request);

        assertThat(existing.getId()).isEqualTo(10L);
        assertThat(existing.getName()).isEqualTo("New name");
        assertThat(existing.getCategory()).isEqualTo("New category");
        assertThat(existing.getPrice()).isEqualByComparingTo("25.50");
        assertThat(existing.getDescription()).isEqualTo("New description");
    }

    @Test
    void toDtoShouldMapAllFieldsFromEntity() {
        Product entity = new Product();
        entity.setId(7L);
        entity.setName("Product");
        entity.setCategory("Category");
        entity.setPrice(new BigDecimal("9.99"));
        entity.setDescription("Description");

        ProductResponseDto dto = ProductMapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.name()).isEqualTo("Product");
        assertThat(dto.category()).isEqualTo("Category");
        assertThat(dto.price()).isEqualByComparingTo("9.99");
        assertThat(dto.description()).isEqualTo("Description");
    }
}
