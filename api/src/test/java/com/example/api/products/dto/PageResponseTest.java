package com.example.api.products.dto;

import com.example.common.product.dto.PageResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void shouldStoreAllValuesCorrectly() {
        var dto = new PageResponse<>(
                List.of("A", "B"),
                2,
                20,
                100L,
                5,
                false
        );

        assertThat(dto.content()).containsExactly("A", "B");
        assertThat(dto.pageNumber()).isEqualTo(2);
        assertThat(dto.pageSize()).isEqualTo(20);
        assertThat(dto.totalElements()).isEqualTo(100L);
        assertThat(dto.totalPages()).isEqualTo(5);
        assertThat(dto.last()).isFalse();
    }
}
