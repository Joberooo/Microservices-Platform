package com.example.api.config;

import com.example.common.CorrelationIdFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdConfigTest {

    @Test
    void shouldCreateCorrelationIdFilterBean() {
        CorrelationIdConfig config = new CorrelationIdConfig();

        CorrelationIdFilter filter = config.correlationIdFilter();

        assertThat(filter).isNotNull();
    }
}
