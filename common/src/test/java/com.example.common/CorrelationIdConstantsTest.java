package com.example.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdConstantsTest {

    @Test
    void shouldExposeExpectedHeaderAndMdcKeys() {
        assertThat(CorrelationIdConstants.HEADER_NAME).isEqualTo("X-Correlation-Id");
        assertThat(CorrelationIdConstants.MDC_KEY).isEqualTo("correlationId");
    }
}
