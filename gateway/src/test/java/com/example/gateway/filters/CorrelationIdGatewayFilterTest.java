package com.example.gateway.filters;

import com.example.common.CorrelationIdConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdGatewayFilterTest {

    private final CorrelationIdGatewayFilter filter = new CorrelationIdGatewayFilter();

    @Test
    void shouldGenerateCorrelationIdIfMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, ex -> {
            ServerHttpRequest mutated = ex.getRequest();
            String header = mutated.getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);
            assertThat(header).isNotBlank();
            assertThat(UUID.fromString(header)).isNotNull(); // valid UUID
            return Mono.empty();
        }).block();

        String responseHeader =
                exchange.getResponse().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);

        assertThat(responseHeader).isNotBlank();
        UUID.fromString(responseHeader);
    }

    @Test
    void shouldPreserveCorrelationIdIfPresent() {
        String cid = "abc-123";

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(CorrelationIdConstants.HEADER_NAME, cid)
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, ex -> {
            String mutatedHeader =
                    ex.getRequest().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);

            assertThat(mutatedHeader).isEqualTo(cid);
            return Mono.empty();
        }).block();

        String responseCid =
                exchange.getResponse().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);

        assertThat(responseCid).isEqualTo(cid);
    }
}
