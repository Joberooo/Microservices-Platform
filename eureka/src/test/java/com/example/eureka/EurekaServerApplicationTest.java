package com.example.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = EurekaServerApplication.class,
        properties = {
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",
                "logging.level.com.netflix.eureka=OFF",
                "logging.level.com.netflix.discovery=OFF"
        }
)
class EurekaServerApplicationTest {

    @Test
    void contextLoads() {
        // If the context fails to start, this test will fail.
        // No additional assertions needed.
    }
}
