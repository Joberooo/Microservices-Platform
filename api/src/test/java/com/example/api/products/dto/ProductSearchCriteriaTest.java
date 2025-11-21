package com.example.api.products.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSearchCriteriaTest {

    @Test
    void constructorShouldStoreFields() {
        var criteria = new ProductSearchCriteria(
                3,
                15,
                List.of("name,desc", "price,asc"),
                "Laptop",
                "Electronics"
        );

        assertThat(criteria.page()).isEqualTo(3);
        assertThat(criteria.size()).isEqualTo(15);
        assertThat(criteria.sort()).containsExactly("name,desc", "price,asc");
        assertThat(criteria.name()).isEqualTo("Laptop");
        assertThat(criteria.category()).isEqualTo("Electronics");
    }
}
