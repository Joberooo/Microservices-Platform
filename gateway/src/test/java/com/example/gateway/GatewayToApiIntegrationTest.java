package com.example.gateway;

import com.example.common.CorrelationIdConstants;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                GatewayApplication.class,
                GatewayToApiIntegrationTest.TestRoutesConfig.class
        },
        properties = {
                "eureka.client.enabled=false"
        }
)
class GatewayToApiIntegrationTest {

    private static final String RESPONSE_BODY = """
            {
              "content": [],
              "pageNumber": 0,
              "pageSize": 10,
              "totalElements": 0,
              "totalPages": 0,
              "last": true
            }
            """;
    private static MockWebServer apiServer;
    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void startMockApi() throws Exception {
        apiServer = new MockWebServer();
        apiServer.start();
    }

    @AfterAll
    static void shutdownMockApi() throws Exception {
        apiServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("test.api.base-url", () -> apiServer.url("/").toString());

        registry.add("rate-limit.capacity", () -> 1);
        registry.add("rate-limit.refill-tokens", () -> 1);
        registry.add("rate-limit.refill-duration", () -> "1s");
    }

    @Test
    void shouldForwardRequestToApiAndPropagateCorrelationId() throws Exception {
        String correlationId = "CID-123";

        apiServer.enqueue(
                new MockResponse()
                        .setBody(RESPONSE_BODY)
                        .addHeader("Content-Type", "application/json")
        );

        webTestClient.get()
                .uri("/api/products?page=0&size=10")
                .header(CorrelationIdConstants.HEADER_NAME, correlationId)
                .header("Authorization", "Bearer dummy-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CorrelationIdConstants.HEADER_NAME, correlationId)
                .expectBody()
                .jsonPath("$.pageNumber").isEqualTo(0)
                .jsonPath("$.pageSize").isEqualTo(10);

        RecordedRequest recorded = apiServer.takeRequest();

        assertThat(recorded.getPath())
                .isEqualTo("/products?page=0&size=10");

        assertThat(recorded.getHeader(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo(correlationId);
    }

    @Test
    void shouldReturn429WhenRateLimitExceeded() {
        String clientIp = "203.0.113.10";

        int before = apiServer.getRequestCount();

        apiServer.enqueue(
                new MockResponse()
                        .setBody(RESPONSE_BODY)
                        .addHeader("Content-Type", "application/json")
        );

        webTestClient.get()
                .uri("/api/products?page=0&size=5")
                .header("X-Forwarded-For", clientIp)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pageNumber").isEqualTo(0)
                .jsonPath("$.pageSize").isEqualTo(10);

        webTestClient.get()
                .uri("/api/products?page=0&size=5")
                .header("X-Forwarded-For", clientIp)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().valueEquals("Retry-After", "1")
                .expectBody().isEmpty();

        int after = apiServer.getRequestCount();
        assertThat(after - before).isEqualTo(1);
    }

    @TestConfiguration
    static class TestRoutesConfig {

        @Bean
        RouteLocator testRouteLocator(RouteLocatorBuilder builder,
                                      org.springframework.core.env.Environment env) {

            String apiBaseUrl = env.getProperty("test.api.base-url");
            if (apiBaseUrl == null) {
                throw new IllegalStateException("test.api.base-url is not set");
            }

            return builder.routes()
                    .route("api-route", r -> r
                            .path("/api/**")
                            .filters(f -> f.stripPrefix(1))
                            .uri(apiBaseUrl))
                    .build();
        }
    }
}
