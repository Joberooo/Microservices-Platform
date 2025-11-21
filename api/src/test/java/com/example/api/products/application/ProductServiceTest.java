package com.example.api.products.application;

import com.example.api.products.client.ProductDbClient;
import com.example.api.products.dto.PageResponse;
import com.example.api.products.dto.ProductDto;
import com.example.api.products.dto.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductDbClient productDbClient;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productDbClient = mock(ProductDbClient.class);
        productService = new ProductService(productDbClient);
    }

    @Test
    void getProductsShouldDelegateToDbClientWithCriteriaFields() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                2,
                25,
                List.of("name,desc", "price,asc"),
                null,
                null
        );

        PageResponse<ProductDto> response = new PageResponse<>(
                List.of(new ProductDto()),
                2,
                25,
                1L,
                1,
                true
        );

        when(productDbClient.getProducts(anyInt(), anyInt(), anyList(), isNull(), isNull()))
                .thenReturn(Mono.just(response));

        PageResponse<ProductDto> result = productService.getProducts(criteria).block();

        assertThat(result).isSameAs(response);

        verify(productDbClient).getProducts(2, 25, criteria.sort(),  null, null);
    }

    @Test
    void getProductsShouldDelegateToDbClientWithFilters() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                0,
                10,
                List.of("name,desc"),
                "Laptop",
                "Electronics"
        );

        PageResponse<ProductDto> response = new PageResponse<>(
                List.of(new ProductDto()),
                0,
                10,
                1L,
                1,
                true
        );

        when(productDbClient.getProducts(0, 10, criteria.sort(), "Laptop", "Electronics"))
                .thenReturn(Mono.just(response));

        PageResponse<ProductDto> result = productService.getProducts(criteria).block();

        assertThat(result).isSameAs(response);

        verify(productDbClient).getProducts(0, 10, criteria.sort(), "Laptop", "Electronics");
    }

    @Test
    void getProductShouldDelegateToDbClient() {
        ProductDto dto = new ProductDto();
        dto.setId(1L);

        when(productDbClient.getProductById(1L)).thenReturn(Mono.just(dto));

        ProductDto result = productService.getProduct(1L).block();

        assertThat(result).isSameAs(dto);
        verify(productDbClient).getProductById(1L);
    }

    @Test
    void createShouldDelegateToDbClient() {
        ProductDto dto = new ProductDto();
        when(productDbClient.createProduct(dto)).thenReturn(Mono.just(dto));

        ProductDto result = productService.create(dto).block();

        assertThat(result).isSameAs(dto);
        verify(productDbClient).createProduct(dto);
    }

    @Test
    void updateShouldDelegateToDbClient() {
        ProductDto dto = new ProductDto();
        when(productDbClient.updateProduct(5L, dto)).thenReturn(Mono.just(dto));

        ProductDto result = productService.update(5L, dto).block();

        assertThat(result).isSameAs(dto);
        verify(productDbClient).updateProduct(5L, dto);
    }

    @Test
    void deleteShouldDelegateToDbClient() {
        when(productDbClient.deleteProduct(3L)).thenReturn(Mono.empty());

        productService.delete(3L).block();

        verify(productDbClient).deleteProduct(3L);
    }
}
