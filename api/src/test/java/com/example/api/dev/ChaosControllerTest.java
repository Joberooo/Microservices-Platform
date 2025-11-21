package com.example.api.dev;

import com.example.api.products.client.ProductDbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChaosControllerTest {

    private WebTestClient webTestClient;
    private ProductDbClient productDbClient;

    @BeforeEach
    void setUp() {
        productDbClient = Mockito.mock(ProductDbClient.class);

        ChaosController controller = new ChaosController(productDbClient);

        webTestClient = WebTestClient
                .bindToController(controller)
                .configureClient()
                .baseUrl("/dev/chaos")
                .build();
    }

    @Test
    void shouldUseDefaultValuesWhenNoParamsProvided() {
        when(productDbClient.induceChaos(100L, 0.5))
                .thenReturn(Mono.just("OK"));

        webTestClient.get()
                .uri("")
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("OK");

        verify(productDbClient).induceChaos(100L, 0.5);
    }

    @Test
    void shouldForwardQueryParamsToProductDbClient() {
        when(productDbClient.induceChaos(3000L, 0.8))
                .thenReturn(Mono.just("OK"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("delayMs", "3000")
                        .queryParam("errorRate", "0.8")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("OK");

        verify(productDbClient).induceChaos(eq(3000L), eq(0.8));
    }
}
