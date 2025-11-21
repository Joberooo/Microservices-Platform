package com.example.common.error;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorDetailsTest {

    @Test
    void shouldExposeFieldErrorsMap() {
        Map<String, String> errors = Map.of(
                "name", "must not be blank",
                "category", "must not be null"
        );

        ValidationErrorDetails details = new ValidationErrorDetails(errors);

        assertThat(details.fieldErrors()).isEqualTo(errors);
    }
}
