package com.example.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaServerApplicationAnnotationTest {

    @Test
    void shouldBeAnnotatedWithEnableEurekaServer() {
        boolean annotated = EurekaServerApplication.class
                .isAnnotationPresent(EnableEurekaServer.class);

        assertThat(annotated).isTrue();
    }
}
