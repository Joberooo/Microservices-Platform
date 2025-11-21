package com.example.api.dev;

import com.example.api.products.client.ProductDbClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "jwt.secret=0123456789_0123456789_0123456789_01"
        }
)
@AutoConfigureMockMvc
class ChaosControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductDbClient productDbClient;

    @Test
    void shouldReturn401WithoutJwt() throws Exception {
        mockMvc.perform(get("/dev/chaos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WithValidJwt() throws Exception {
        mockMvc.perform(
                        get("/dev/chaos")
                                .with(SecurityMockMvcRequestPostProcessors.jwt())
                )
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ProductDbClient productDbClient() {
            ProductDbClient mock = Mockito.mock(ProductDbClient.class);
            Mockito.when(mock.induceChaos(anyLong(), anyDouble()))
                    .thenReturn(reactor.core.publisher.Mono.just("OK"));
            return mock;
        }

        @Bean
        ChaosController chaosController(ProductDbClient client) {
            return new ChaosController(client);
        }
    }
}
