package com.example.db.dev;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaosServiceTest {

    private final ChaosService service = new ChaosService();

    @Test
    void shouldReturnOkWhenNoError() throws InterruptedException {
        assertThat(service.induceChaos(0, 0.0)).isEqualTo("OK");
    }

    @Test
    void shouldThrowWhenErrorRate1() {
        assertThatThrownBy(() -> service.induceChaos(0, 1.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chaos induced error");
    }
}
