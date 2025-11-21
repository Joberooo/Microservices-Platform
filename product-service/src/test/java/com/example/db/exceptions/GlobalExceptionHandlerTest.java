package com.example.db.exceptions;

import com.example.common.CorrelationIdConstants;
import com.example.common.error.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleProductNotFoundShouldReturn404WithApiErrorResponse() {
        ProductNotFoundException ex = new ProductNotFoundException(42L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.addHeader(CorrelationIdConstants.HEADER_NAME, "corr-id-123");

        ResponseEntity<ApiErrorResponse> response = handler.handleProductNotFound(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        ApiErrorResponse body = response.getBody();
        assertThat(body).as("Body must not be null").isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).contains("42");
        assertThat(body.path()).isEqualTo("/products/42");
        assertThat(body.correlationId()).isEqualTo("corr-id-123");
        assertThat(body.fieldErrors()).isNull();
    }

    @Test
    void handleGenericShouldReturn500WithMessage() {
        RuntimeException ex = new RuntimeException("Something went wrong");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/any");

        ResponseEntity<ApiErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApiErrorResponse body = response.getBody();
        assertThat(body).as("Body must not be null").isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("Something went wrong");
        assertThat(body.path()).isEqualTo("/any");
        assertThat(body.correlationId()).isEqualTo("N/A");
    }
}
