package com.example.api.config;

import com.example.common.CorrelationIdConstants;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

    private MockWebServer server;

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
        MDC.clear();
    }

    @Test
    void shouldPropagateCorrelationIdHeader() throws Exception {
        MDC.put(CorrelationIdConstants.MDC_KEY, "CID-123");

        WebClientConfig config = new WebClientConfig();
        ExchangeFilterFunction cidFilter = config.correlationIdClientFilter();

        WebClient client = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .filter(cidFilter)
                .build();

        server.enqueue(new MockResponse().setBody("OK").setResponseCode(200));

        String response = client.get().uri("/").retrieve().bodyToMono(String.class).block();

        assertThat(response).isEqualTo("OK");

        var recorded = server.takeRequest();
        assertThat(recorded.getHeader(CorrelationIdConstants.HEADER_NAME))
                .isEqualTo("CID-123");
    }
}
