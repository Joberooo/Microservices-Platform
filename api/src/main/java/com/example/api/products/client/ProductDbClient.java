package com.example.api.products.client;

import com.example.api.products.dto.ProductDto;
import com.example.common.CorrelationIdConstants;
import com.example.common.error.ApiErrorResponse;
import com.example.common.product.dto.PageResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDbClient {

    private static final String SERVICE_NAME = "product-service";

    private final WebClient.Builder webClientBuilder;

    private WebClient webClient() {
        return webClientBuilder
                .baseUrl("http://" + SERVICE_NAME)
                .build();
    }

    private String resolveCorrelationId() {
        String cid = MDC.get(CorrelationIdConstants.MDC_KEY);
        return cid != null ? cid : "";
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        HttpStatusCode statusCode = response.statusCode();

        return response.bodyToMono(ApiErrorResponse.class)
                .defaultIfEmpty(ApiErrorResponse.builder()
                        .status(statusCode.value())
                        .error(statusCode.toString())
                        .message("Upstream product-service error")
                        .build())
                .flatMap(error -> {
                    HttpStatus status = HttpStatus.resolve(statusCode.value());
                    if (status == null) {
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                    }

                    String message = error.message() != null
                            ? error.message()
                            : "Error when calling product-service";

                    log.warn("Error from product-service: status={}, message={}", status, message);

                    return Mono.error(new ResponseStatusException(status, message));
                });
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<PageResponse<ProductDto>> getProducts(int page, int size, List<String> sort, String name, String category) {
        return webClient()
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/products")
                            .queryParam("page", page)
                            .queryParam("size", size);
                    if (sort != null && !sort.isEmpty()) {
                        sort.forEach(s -> builder.queryParam("sort", s));
                    }
                    if (name != null && !name.isBlank()) {
                        builder.queryParam("name", name);
                    }
                    if (category != null && !category.isBlank()) {
                        builder.queryParam("category", category);
                    }
                    return builder.build();
                })
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(new ParameterizedTypeReference<>() {
                });
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<ProductDto> getProductById(Long id) {
        return webClient()
                .get()
                .uri("/products/{id}", id)
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(ProductDto.class);
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<ProductDto> createProduct(ProductDto dto) {
        return webClient()
                .post()
                .uri("/products")
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(ProductDto.class);
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<ProductDto> updateProduct(Long id, ProductDto dto) {
        return webClient()
                .put()
                .uri("/products/{id}", id)
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(ProductDto.class);
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<Void> deleteProduct(Long id) {
        return webClient()
                .delete()
                .uri("/products/{id}", id)
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(Void.class);
    }

    @CircuitBreaker(name = "dbService")
    @Retry(name = "dbService")
    public Mono<String> induceChaos(long delayMs, double errorRate) {
        return webClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/dev/chaos")
                        .queryParam("delayMs", delayMs)
                        .queryParam("errorRate", errorRate)
                        .build())
                .header(CorrelationIdConstants.HEADER_NAME, resolveCorrelationId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(String.class);
    }
}
