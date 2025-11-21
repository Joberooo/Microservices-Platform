package com.example.api.products.mapper;

import com.example.api.products.dto.ProductDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    @Test
    void copyShouldReturnNullWhenSourceIsNull() {
        ProductDto copy = ProductMapper.copy(null);
        assertThat(copy).isNull();
    }

    @Test
    void copyShouldCreateDeepCopyOfAllFields() {
        ProductDto original = new ProductDto();
        original.setId(1L);
        original.setName("Name");
        original.setCategory("Category");
        original.setPrice(new BigDecimal("10.50"));
        original.setDescription("Description");

        ProductDto copy = ProductMapper.copy(original);

        assertThat(copy).isNotNull();
        assertThat(copy.getId()).isEqualTo(1L);
        assertThat(copy.getName()).isEqualTo("Name");
        assertThat(copy.getCategory()).isEqualTo("Category");
        assertThat(copy.getPrice()).isEqualByComparingTo("10.50");
        assertThat(copy.getDescription()).isEqualTo("Description");

        assertThat(copy).isNotSameAs(original);
    }
}
