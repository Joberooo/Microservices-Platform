package com.example.db.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @Positive BigDecimal price,
        String description
) {}
