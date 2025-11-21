package com.example.api.products.web;

import com.example.api.products.application.ProductService;
import com.example.api.products.dto.ProductDto;
import com.example.api.products.dto.ProductSearchCriteria;
import com.example.common.product.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    private WebTestClient webTestClient;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        ProductController controller = new ProductController(productService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    private ProductDto sampleProduct() {
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("Sample");
        dto.setCategory("Category");
        dto.setPrice(new BigDecimal("10.00"));
        dto.setDescription("Description");
        return dto;
    }

    @Test
    void getProductsShouldCallServiceWithSearchCriteria() {
        ProductDto dto = sampleProduct();
        PageResponse<ProductDto> pageResponse = new PageResponse<>(
                List.of(dto),
                0,
                10,
                1L,
                1,
                true
        );

        when(productService.getProducts(any(ProductSearchCriteria.class)))
                .thenReturn(Mono.just(pageResponse));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .queryParam("sort", "name,desc", "price,asc")
                        .queryParam("name", "Laptop")
                        .queryParam("category", "Electronics")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(1)
                .jsonPath("$.content[0].name").isEqualTo("Sample")
                .jsonPath("$.pageNumber").isEqualTo(0)
                .jsonPath("$.pageSize").isEqualTo(10)
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.totalPages").isEqualTo(1)
                .jsonPath("$.last").isEqualTo(true);

        ArgumentCaptor<ProductSearchCriteria> captor =
                ArgumentCaptor.forClass(ProductSearchCriteria.class);
        verify(productService).getProducts(captor.capture());

        ProductSearchCriteria criteria = captor.getValue();
        assertThat(criteria.page()).isEqualTo(0);
        assertThat(criteria.size()).isEqualTo(10);
        assertThat(criteria.sort()).containsExactly("name,desc", "price,asc");
        assertThat(criteria.name()).isEqualTo("Laptop");
        assertThat(criteria.category()).isEqualTo("Electronics");
    }

    @Test
    void getByIdShouldReturnSingleProduct() {
        ProductDto dto = sampleProduct();
        when(productService.getProduct(1L)).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri("/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Sample");

        verify(productService).getProduct(1L);
    }

    @Test
    void createShouldReturn201WithCreatedProduct() {
        ProductDto dto = sampleProduct();
        dto.setId(null);

        ProductDto saved = sampleProduct();
        when(productService.create(dto)).thenReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Sample");

        verify(productService).create(dto);
    }

    @Test
    void updateShouldReturn200WithUpdatedProduct() {
        ProductDto dto = sampleProduct();
        dto.setName("Updated");

        when(productService.update(1L, dto)).thenReturn(Mono.just(dto));

        webTestClient.put()
                .uri("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Updated");

        verify(productService).update(1L, dto);
    }

    @Test
    void deleteShouldReturn204() {
        when(productService.delete(1L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/products/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(productService).delete(1L);
    }
}
