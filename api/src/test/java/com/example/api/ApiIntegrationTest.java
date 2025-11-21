package com.example.api;

import com.example.api.products.client.ProductDbClient;
import com.example.api.products.dto.ProductDto;
import com.example.common.product.dto.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static com.example.api.testutil.JwtTestUtils.createTestJwt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "jwt.secret=0123456789_0123456789_0123456789_01"
        }
)
class ApiIntegrationTest {

    private static final String JWT = createTestJwt();
    @Autowired
    WebTestClient webTestClient;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    ProductDbClient productDbClient;

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
    void shouldReturn401WithoutJwt() {
        webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnPagedProductsWithJwt() {
        ProductDto dto = sampleProduct();

        PageResponse<ProductDto> response = new PageResponse<>(
                List.of(dto),
                0,
                10,
                1,
                1,
                true
        );

        when(productDbClient.getProducts(anyInt(), anyInt(), anyList(), isNull(), isNull()))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uri -> uri
                        .path("/products")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .queryParam("sort", "name,desc")
                        .build())
                .header("Authorization", "Bearer " + JWT)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].id").isEqualTo(1)
                .jsonPath("$.content[0].name").isEqualTo("Sample");

        ArgumentCaptor<Integer> page = ArgumentCaptor.forClass(Integer.class);
        verify(productDbClient).getProducts(page.capture(), anyInt(), anyList(), isNull(), isNull());
        assertThat(page.getValue()).isEqualTo(0);
    }

    @Test
    void shouldGetSingleProductWithJwt() {
        ProductDto dto = sampleProduct();
        when(productDbClient.getProductById(42L)).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri("/products/42")
                .header("Authorization", "Bearer " + JWT)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Sample");

        verify(productDbClient).getProductById(42L);
    }

    @Test
    void shouldCreateProductWithJwt() throws Exception {
        ProductDto input = sampleProduct();
        input.setId(null);

        ProductDto output = sampleProduct();

        when(productDbClient.createProduct(input)).thenReturn(Mono.just(output));

        webTestClient.post()
                .uri("/products")
                .header("Authorization", "Bearer " + JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(input))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1);

        verify(productDbClient).createProduct(input);
    }

    @Test
    void shouldUpdateProductWithJwt() throws Exception {
        ProductDto input = sampleProduct();
        input.setName("Updated");

        when(productDbClient.updateProduct(5L, input))
                .thenReturn(Mono.just(input));

        webTestClient.put()
                .uri("/products/5")
                .header("Authorization", "Bearer " + JWT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(input))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated");

        verify(productDbClient).updateProduct(5L, input);
    }

    @Test
    void shouldDeleteProductWithJwt() {
        when(productDbClient.deleteProduct(7L)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/products/7")
                .header("Authorization", "Bearer " + JWT)
                .exchange()
                .expectStatus().isNoContent();

        verify(productDbClient).deleteProduct(7L);
    }

    @Test
    void shouldCallChaosEndpointWithJwtAndDelegateToProductDbClient() {
        long delayMs = 250L;
        double errorRate = 0.3;

        when(productDbClient.induceChaos(delayMs, errorRate))
                .thenReturn(Mono.just("CHAOS_OK"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/dev/chaos")
                        .queryParam("delayMs", delayMs)
                        .queryParam("errorRate", errorRate)
                        .build())
                .header("Authorization", "Bearer " + JWT)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("CHAOS_OK");

        verify(productDbClient).induceChaos(delayMs, errorRate);
    }
}
