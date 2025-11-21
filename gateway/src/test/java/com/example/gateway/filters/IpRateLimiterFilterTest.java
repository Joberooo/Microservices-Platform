package com.example.gateway.filters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class IpRateLimiterFilterTest {

    private IpRateLimiterFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IpRateLimiterFilter(2, 1, java.time.Duration.ofSeconds(1));
    }

    @Test
    void shouldAllowRequestsUntilTokensExhausted() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isNull();

        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isNull();

        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        filter.filter(exchange, e -> Mono.empty()).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void shouldUseXRealIpIfPresent() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .header("X-Real-IP", "10.1.1.1")
                        .build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isNull(); // allowed
    }

    @Test
    void shouldUseXForwardedForIfPresent() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/")
                        .header("X-Forwarded-For", "192.168.1.1")
                        .build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull(); // allowed
    }

    @Test
    void shouldReturnRateLimitHeadersWhenAllowed() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        filter.filter(exchange, e -> Mono.empty()).block();

        var headers = exchange.getResponse().getHeaders();

        assertThat(headers.getFirst("X-RateLimit-Limit")).isEqualTo("2");
        assertThat(headers.getFirst("X-RateLimit-Remaining")).isNotNull();
    }

    @Test
    void shouldReturnRetryAfterHeaderWhenBlocked() {
        filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/").build()), e -> Mono.empty()).block();
        filter.filter(MockServerWebExchange.from(MockServerHttpRequest.get("/").build()), e -> Mono.empty()).block();

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());

        filter.filter(exchange, e -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exchange.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("1");
    }
}
