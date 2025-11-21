package com.example.common.error;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseTest {

    @Test
    void builderShouldPopulateAllFields() {
        Instant now = Instant.now();
        Map<String, String> fieldErrors = Map.of(
                "name", "must not be blank",
                "price", "must be greater than zero"
        );

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Validation failed")
                .path("/products")
                .correlationId("cid-123")
                .fieldErrors(fieldErrors)
                .build();

        assertThat(response.timestamp()).isEqualTo(now);
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.message()).isEqualTo("Validation failed");
        assertThat(response.path()).isEqualTo("/products");
        assertThat(response.correlationId()).isEqualTo("cid-123");
        assertThat(response.fieldErrors()).isEqualTo(fieldErrors);
    }
}
