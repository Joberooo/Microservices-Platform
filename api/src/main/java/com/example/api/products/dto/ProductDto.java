package com.example.api.products.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String category;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal price;

    @Size(max = 1024)
    private String description;
}
