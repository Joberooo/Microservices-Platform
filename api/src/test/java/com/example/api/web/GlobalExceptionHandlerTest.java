package com.example.api.web;

import com.example.common.CorrelationIdConstants;
import com.example.common.error.ApiErrorResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldReturnBadRequestWithFieldErrors_WhenValidationFails() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products");

        var targetObject = new Object();
        var bindingResult = new BeanPropertyBindingResult(targetObject, "productDto");

        bindingResult.addError(new FieldError("productDto", "name", "must not be blank"));
        bindingResult.addError(new FieldError("productDto", "price", "must be greater than zero"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);

        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.error()).isEqualTo("Bad Request");
        assertThat(body.message()).isEqualTo("Validation failed");
        assertThat(body.path()).isEqualTo("/products");

        assertThat(body.fieldErrors()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "name", "must not be blank",
                "price", "must be greater than zero"
        ));
    }

    @Test
    void shouldReturnProperResponse_WhenResponseStatusExceptionThrown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/products/42");
        request.addHeader(CorrelationIdConstants.HEADER_NAME, "CID-123");

        ResponseStatusException ex =
                new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Product missing");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleResponseStatus(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);

        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.message()).isEqualTo("Product missing");
        assertThat(body.path()).isEqualTo("/products/42");
        assertThat(body.correlationId()).isEqualTo("CID-123");
        assertThat(body.fieldErrors()).isNull();
    }

    @Test
    void shouldReturnInternalServerError_WhenGenericExceptionOccurs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/unexpected");
        MDC.put(CorrelationIdConstants.MDC_KEY, "CID-XYZ");

        Exception ex = new RuntimeException("Boom");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);

        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).isEqualTo("Boom");
        assertThat(body.path()).isEqualTo("/unexpected");
        assertThat(body.correlationId()).isEqualTo("CID-XYZ");
        assertThat(body.fieldErrors()).isNull();
    }
}
