package com.example.db.product.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long id,
        String name,
        String category,
        BigDecimal price,
        String description
) {}
