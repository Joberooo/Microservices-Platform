package com.example.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.common.CorrelationIdConstants.HEADER_NAME;
import static com.example.common.CorrelationIdConstants.MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldPropagateExistingCorrelationIdFromHeaderToMdcAndResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String existingId = "cid-123";

        request.addHeader(HEADER_NAME, existingId);

        AtomicReference<String> correlationInChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> correlationInChain.set(MDC.get(MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(correlationInChain.get()).isEqualTo(existingId);
        assertThat(response.getHeader(HEADER_NAME)).isEqualTo(existingId);
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> correlationInChain = new AtomicReference<>();

        FilterChain chain = (req, res) -> correlationInChain.set(MDC.get(MDC_KEY));

        filter.doFilter(request, response, chain);

        String generatedId = correlationInChain.get();
        assertThat(generatedId).isNotNull().isNotBlank();

        assertThat(response.getHeader(HEADER_NAME)).isEqualTo(generatedId);
        assertThat(MDC.get(MDC_KEY)).isNull();
    }
}
