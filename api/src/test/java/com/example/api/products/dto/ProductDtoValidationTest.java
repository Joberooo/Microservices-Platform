package com.example.api.products.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ProductDto valid() {
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("Valid name");
        dto.setCategory("Category");
        dto.setPrice(new BigDecimal("10.00"));
        dto.setDescription("OK");
        return dto;
    }

    @Test
    void validDtoShouldHaveNoViolations() {
        var dto = valid();
        Set<?> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void nameShouldNotBeBlank() {
        var dto = valid();
        dto.setName("   ");

        var violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void categoryShouldNotBeBlank() {
        var dto = valid();
        dto.setCategory("");

        var violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("category"));
    }

    @Test
    void priceShouldNotBeNull() {
        var dto = valid();
        dto.setPrice(null);

        var violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    void priceShouldBeGreaterOrEqualZero() {
        var dto = valid();
        dto.setPrice(new BigDecimal("-1.00"));

        var violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    void descriptionShouldNotExceedMaxLength() {
        var dto = valid();
        dto.setDescription("A".repeat(2000)); // exceeds 1024

        var violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }
}
