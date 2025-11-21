package com.example.api.products.client;

import com.example.api.products.dto.ProductDto;
import com.example.common.CorrelationIdConstants;
import com.example.common.error.ApiErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductDbClientTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void deleteProductShouldPropagateCorrelationIdHeader() {
        String correlationId = "cid-test-123";
        MDC.put(CorrelationIdConstants.MDC_KEY, correlationId);

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            ClientResponse response = ClientResponse
                    .create(HttpStatus.NO_CONTENT)
                    .build();
            return Mono.just(response);
        };

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(exchangeFunction);

        ProductDbClient client = new ProductDbClient(builder);

        client.deleteProduct(1L).block();

        ClientRequest request = capturedRequest.get();
        assertThat(request).isNotNull();
        assertThat(request.headers().getFirst(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo(correlationId);
    }

    @Test
    void getProductByIdShouldMapApiErrorResponseToResponseStatusException() {
        ApiErrorResponse apiError = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(404)
                .error("Not Found")
                .message("Product not found")
                .path("/products/42")
                .correlationId("cid-err-123")
                .fieldErrors(null)
                .build();

        ExchangeFunction exchangeFunction = getExchangeFunction(apiError);

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(exchangeFunction);

        ProductDbClient client = new ProductDbClient(builder);

        Mono<ProductDto> result = client.getProductById(42L);

        assertThatThrownBy(result::block)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(404);
                    assertThat(rse.getReason()).isEqualTo("Product not found");
                });
    }

    @Test
    void getProductsShouldIncludeFilterQueryParams() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);

            String json = """
                {
                  "content": [],
                  "pageNumber": 0,
                  "pageSize": 10,
                  "totalElements": 0,
                  "totalPages": 0,
                  "last": true
                }
                """;

            ClientResponse response = ClientResponse
                    .create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();

            return Mono.just(response);
        };

        WebClient.Builder builder = WebClient.builder()
                .exchangeFunction(exchangeFunction);

        ProductDbClient client = new ProductDbClient(builder);

        client.getProducts(
                0,
                10,
                java.util.List.of("name,desc"),
                "chair",
                "Electronics"
        ).block();

        ClientRequest request = capturedRequest.get();
        assertThat(request).isNotNull();

        String query = request.url().getQuery();
        assertThat(query).contains("page=0");
        assertThat(query).contains("size=10");
        assertThat(query).contains("sort=name,desc");
        assertThat(query).contains("name=chair");
        assertThat(query).contains("category=Electronics");
    }

    @NotNull
    private static ExchangeFunction getExchangeFunction(ApiErrorResponse apiError) {
        String json = """
                {
                  "timestamp": "%s",
                  "status": 404,
                  "error": "Not Found",
                  "message": "Product not found",
                  "path": "/products/42",
                  "correlationId": "cid-err-123",
                  "fieldErrors": null
                }
                """.formatted(apiError.timestamp().toString());

        return request -> {
            ClientResponse response = ClientResponse
                    .create(HttpStatus.NOT_FOUND)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();
            return Mono.just(response);
        };
    }
}
