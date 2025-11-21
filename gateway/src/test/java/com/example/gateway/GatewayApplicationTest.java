package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootTest(
        classes = {
                GatewayApplication.class,
                GatewayApplicationTest.TestRoutesConfig.class
        },
        properties = {
                "eureka.client.enabled=false"
        }
)
class GatewayApplicationTest {

    @Test
    void contextLoads() {
        // If the Spring context fails to start, this test will fail.
        // No additional assertions are needed here.
    }

    @TestConfiguration
    static class TestRoutesConfig {

        @Bean
        RouteLocator testRouteLocator(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("test-route", r -> r
                            .path("/test/**")
                            .uri("http://example.org"))
                    .build();
        }
    }
}
